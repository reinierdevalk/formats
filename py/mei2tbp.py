import copy
import json
import os
import sys
import xml.etree.ElementTree as ET
#from itertools import islice
#from sys import argv


# Ensure that Python can find .py files in utils/py/ regardless of where the script
# is run from by adding the path holding the code (<lib_path>) to sys.path
# __file__ 					= <lib_path>/formats/py/mei2tbp.py
# os.path.dirname(__file__) = <lib_path>/formats/py/
# '../../' 					= up two levels, i.e., <lib_path>
lib_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../utils'))
if lib_path not in sys.path:
	sys.path.insert(0, lib_path)


from py.constants import *
from py.utils import get_tuning, add_unique_id, handle_namespaces, parse_tree, write_xml, print_all_elements, print_all_labelled_elements

_, inpath, infile = sys.argv


# NB Must be the same as in formats.tbp.symbols.Symbol
MENSURATION_SIGNS = {'2:4': 'M2', '2:4_num': 'M2', 
					 '3:4': 'M3', '3:4_num': 'M3', '3:4_sym_O': 'MO', 
					 '4:4': 'M4', '4:4_common': 'MC',
					 '5:4': 'M5', '5:4_num': 'M5',
					 '6:4': 'M6', '6:4_num': 'M6',
					 '7:4': 'M7', '7:4_num': 'M7', 
					 '2:2_cut': 'MC\\'
					}
# NB Must be the same as in formats.tbp.symbols.Symbol
BARLINES = {None: '|',
			'single': '|',
			'dbl': '||',
			'end': '||',
			'rptstart': '||:',
			'rptboth': ':||:',
			'rptend': ':||'
		   }
# NB Must be the same as in formats.tbp.symbols.Symbol
DURATIONS = {'1': 'br', '2': 'sb', '4': 'mi', '8': 'sm', '16': 'fu', '32': 'sf'}
# NB Must be the same as in formats.tbp.symbols.TabSymbol
NEWSIDLER = [['5', 'e', 'k', 'p', 'v', '9', 'e-', 'k-', 'p-', 'v-', '9-'], 
			 ['4', 'd', 'i', 'o', 't', '7', 'd-', 'i-', 'o-', 't-', '7-'],
			 ['3', 'c', 'h', 'n', 's', 'z', 'c-', 'h-', 'n-', 's-', 'z-'],
			 ['2', 'b', 'g', 'm', 'r', 'y', 'b-', 'g-', 'm-', 'r-', 'y-'],
			 ['1', 'a', 'f', 'l', 'q', 'x', 'a-', 'f-', 'l-', 'q-', 'x-'],
			 ['+', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K']
			]

NUM_COURSES = 6
LEN_ID = 8 # TODO move to utils module together with duplicate functions w/ diplomat
TAG = 'corr' # TODO make argument
#TAG = 'sic'
URI_MEI = None
URI_XML = None
XML_ID_KEY = None
NOT_TYPE = None
xml_ids = None


def split_multi_measure_choice(root: ET.Element): # -> None
	"""
	Splits any successive <measure>s wrapped in a single <choice> each into their own <choice>, i.e.,
	
	<choice>
		<corr>
			<measure n='1'/>
			<measure n='2'/>
		</corr>
		<sic>
			<measure n='1'/>
			<measure n='2'/>
		</sic>
	</choice>

	becomes

	<choice>
		<corr>
			<measure n='1'/>
		</corr>
		<sic>
			<measure n='1'/>
		</sic>
	</choice>
	<choice>
		<corr>
			<measure n='2'/>
		</corr>
		<sic>
			<measure n='2'/>
		</sic>
	</choice>   
	"""
	replacements = []

	for parent in root.iter():
		for i, child in enumerate(list(parent)):
			if child.tag == f'{URI_MEI}choice':
				corr = child.find('mei:corr', ns)
				sic = child.find('mei:sic', ns)

				if corr is None or sic is None:
					continue

				corr_measures = list(corr.findall('mei:measure', ns))
				sic_measures = list(sic.findall('mei:measure', ns))
				if len(corr_measures) != len(sic_measures):
					raise ValueError('Mismatch in number of <measure> elements in <corr> and <sic>')

				choices_split = []
				for corr_m, sic_m in zip(corr_measures, sic_measures):
					if corr_m.get('n') != sic_m.get('n'):
						raise ValueError(f'Mismatch in measure number (@n) values in <corr> and <sic>:\
										 {corr_m.get('n')} != {sic_m.get('n')}')
					curr_corr = ET.Element(f'{URI_MEI}corr', attrib={**corr.attrib})
					curr_corr.append(copy.deepcopy(corr_m))
					curr_sic = ET.Element(f'{URI_MEI}sic', attrib={**sic.attrib})
					curr_sic.append(copy.deepcopy(sic_m))
					curr_choice = ET.Element(f'{URI_MEI}choice', attrib={**child.attrib})					
					curr_choice.set(XML_ID_KEY, add_unique_id('c', xml_ids)[-1])
					curr_choice.append(curr_corr)
					curr_choice.append(curr_sic)
					choices_split.append(curr_choice)

				if choices_split:
					replacements.append((parent, i, child, choices_split))

	# Perform replacements (in reverse to avoid index shifting)
	for parent, i, choice_comb, choices_split in reversed(replacements):
		for offset, c in enumerate(choices_split):
			parent.insert(i + offset, c)
		parent.remove(choice_comb)


def implement_choice(root: ET.Element, choice_ids: list, tag: str): # -> None
	replacements = []

	for elem in root.iter():
		for i, child in enumerate(list(elem)):
			if child.tag == f'{URI_MEI}choice':
				choice_id = child.get(XML_ID_KEY)
				if choice_id in choice_ids:
					choice_elem = child.find(f'mei:{tag}', ns)
					if choice_elem is not None:
						# Deepcopy to avoid modifying original
						choice_elem_copy = copy.deepcopy(choice_elem)

						# Count total <tabGrp> for label logic
						num_tabGrps = len(choice_elem_copy.findall('.//mei:tabGrp', ns))
						tabGrp_cnt = 1

						# Recursively walk all elements and annotate
						lbl_txt = f'from <choice> with @xml:id {choice_id}'
						for sub in choice_elem_copy.iter():
							if sub.tag == f'{URI_MEI}tabGrp':
								pos = 'first' if tabGrp_cnt == 1 else 'following'
								sub.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> {lbl_txt}')
								tabGrp_cnt += 1
							elif sub.tag == f'{URI_MEI}scoreDef':
								sub.set('label', f'<scoreDef> {lbl_txt}')

						# Only insert the top-level children of <corr>/<sic>
						new_elems = list(choice_elem_copy)

						replacements.append((elem, i, child, new_elems))

	# Insert and remove <choice> (in reverse to avoid index shifting)
	for elem, i, choice_elem, new_elems in reversed(replacements):
		for offset, new_elem in enumerate(new_elems):
			elem.insert(i + offset, new_elem)
		elem.remove(choice_elem)


def insert_footnote(choices: list, tbp_events: str, lbl: str, is_single_tg_in_beam: bool, 
					is_ts_event: bool): # -> str

	if (is_ts_event and 'first' in lbl) or not is_ts_event:
		choice_id = lbl.strip().split()[-1]
		c = choices[choice_id]
		ftnt_events = _handle_alt(c, choices, is_ts_event)
		# A single <tabGrp> in a <beam> is wrapped directly in <choice> (and not 
		# first in <beam>), meaning that the MS will not yet be beamed
		if is_ts_event and is_single_tg_in_beam:
			ms = ftnt_events[:ftnt_events.index('.')]
			ftnt_events = ftnt_events.replace(ms, f'{ms}-')
		# Do not include closing '>.'		
		ftnt_events = ftnt_events[:-2]
		ftnt = f'{{@\'{ftnt_events}\' {'in source' if TAG == 'corr' else 'corrected'}}}'
	if is_ts_event and 'following' in lbl:
		ftnt = '{@}'
	tbp_events = tbp_events[:-3] + ftnt + tbp_events[-3:] # -3 is the length of '.>.'

	return tbp_events


def _handle_alt(choice: ET.Element, choices: list, is_ts_event: bool): # -> str
	tbp_events = ''

	alt = choice.find(f'mei:{'sic' if TAG == 'corr' else 'corr'}', ns)

	# If alt contains <measure> as its first (and only) direct child: move this 
	# <measure>'s <layer> content as direct children of alt and remove it from alt 
	first_child = list(alt)[0]
	if first_child.tag == f'{URI_MEI}measure':
		layer = first_child.find('.//mei:layer', ns)
		for child in list(layer):
			alt.append(copy.deepcopy(child))
		alt.remove(first_child)

	# TS event
	if is_ts_event:
		# Possible direct children: <beam>, <tabGrp>, <sb>
		for j, elem in enumerate(list(alt)):
			if elem.tag == f'{URI_MEI}beam':
				tbp_events += handle_beam(elem, choices)
			if elem.tag == f'{URI_MEI}tabGrp':
				tbp_events += convert_tabGrp(elem, False)#j != len(alt)-1)
			elif elem.tag == f'{URI_MEI}sb':
				tbp_events += '\n/\n'
	# MS event
	else:
		tbp_events = convert_meterSig(alt.find('.//mei:meterSig', ns))

	return tbp_events


def convert_meterSig(meterSig: str): # -> str
	count = meterSig.get('count')
	unit = meterSig.get('unit')
	form = meterSig.get('form')
	sym = meterSig.get('sym')

	# @count, @unit
	ms_key = f'{count}:{unit}'
	# @form='num'
	if form is not None:
		ms_key += f'_{form}'
	# @sym='cut', @sym='common'
	if sym is not None:
		ms_key += f'_{sym}'

	return f'{MENSURATION_SIGNS[ms_key]}.>.'


def convert_tabGrp(tabGrp: ET.Element, is_beamed: bool): # -> str
	# Determine RhythmSymbol
	rs = ''
	if tabGrp.find('mei:tabDurSym', ns) is not None:
		dur = tabGrp.get('dur')
		dot = '*' if tabGrp.get('dots') is not None else ''
		beam = '-' if is_beamed else ''
		rs = f'{DURATIONS[dur]}{dot}{beam}.'  

	# Determine TabSymbols
	ts_per_course = [None, None, None, None, None, None] # first element is lowest-sounding course
	for elem in tabGrp:
		if elem.tag == f'{URI_MEI}note':
			fret = elem.get('tab.fret')
			course = elem.get('tab.course')
			if NOT_TYPE == NOTATIONTYPES[FLT]:
				ts = f'{"abcdefghikl"[int(fret)]}{course}.'
			elif NOT_TYPE == NOTATIONTYPES[GLT]:
				ts = f'{NEWSIDLER[int(course)-1][int(fret)]}.'
			else:
				ts = f'{fret}{course}.'
			ts_per_course[NUM_COURSES - int(course)] = ts
	
	# Make event		
	tss = ''
	for ts in ts_per_course:
		if ts is not None:
			tss += ts

	return f'{rs}{tss}>.'


def handle_beam(beam: ET.Element, choices: list): # -> str
	tbp_events = ''

	# Possible direct children in <beam>: <tabGrp>, <sb>
	b_contents = list(beam)
	for i, elem in enumerate(b_contents):
		if elem.tag == f'{URI_MEI}tabGrp':
			# 1. Make event 
			is_beamed = i != len(b_contents) - 1
			tbp_event = convert_tabGrp(elem, is_beamed)

			# 2. Insert footnote
			lbl = elem.get('label')
			if lbl is not None and '<choice>' in lbl:
				is_single_tg_in_beam = True if ('(1/1)' in lbl and is_beamed) else False
				tbp_event = insert_footnote(choices, tbp_event, lbl, is_single_tg_in_beam, True)
			tbp_events += tbp_event
		elif elem.tag == f'{URI_MEI}sb':
			tbp_events += '\n/\n'

	return tbp_events


def handle_measure(measure: ET.Element, choices: list): # -> str
	tbp_events = ''

	# Possible direct children in <layer>: <beam>, <tabGrp>, <sb>
	layer = measure.find('.//mei:layer', ns)
	for elem in layer:
		if elem.tag == f'{URI_MEI}beam':
			tbp_events += handle_beam(elem, choices)
		elif elem.tag == f'{URI_MEI}tabGrp':
			# 1. Make event
			tbp_event = convert_tabGrp(elem, False)
			
			# 2. Insert footnote
			tg_lbl = elem.get('label')
			if tg_lbl is not None and '<choice>' in tg_lbl:
				tbp_event = insert_footnote(choices, tbp_event, tg_lbl, False, True)
			tbp_events += tbp_event
		elif elem.tag == f'{URI_MEI}sb':
			tbp_events += '\n/\n'
	barline = measure.get('right')
	if barline != 'invis':
		tbp_events += f'{BARLINES[barline]}.'
	tbp_events += '\n'

#	# 2. Insert footnote (for whole <measure>) 
#	#    NB Applies only if <choice> contains the whole <measure>, in which 
#	#    case <measure> itself will not contain any further <choice>)
#	m_lbl = measure.get('label') # m_lbl is on <measure>
#	if m_lbl is not None and '<choice>' in m_lbl:
#		print('JA3')
#		print(tbp_events)
#		print('-->' + m_lbl + '<--')
#		tbp_events = insert_footnote(choices, tbp_events, m_lbl, False, True)	
	
	return tbp_events


def handle_scoreDef(scoreDef: ET.Element, choices: list): # -> str
	tbp_event = ''

	meterSig = scoreDef.find('.//mei:meterSig', ns)
	if meterSig is not None:
		# 1. Make event
		tbp_event = convert_meterSig(meterSig) 
		
		# 2. Insert footnote
		lbl = scoreDef.get('label') # lbl is on <scoreDef> as <choice> cannot contain <scoreDef> children 
		if lbl is not None and '<choice>' in lbl:
			tbp_event = insert_footnote(choices, tbp_event, lbl, False, False)

	return tbp_event


def handle_section(section: ET.Element, choices: list): # -> str
	res = ''

	# Possible direct children in <section>: <scoreDef>, <measure>, <sb>
	for elem in section:
		if elem.tag == f'{URI_MEI}scoreDef':
			res += handle_scoreDef(elem, choices)
		elif elem.tag == f'{URI_MEI}measure':
			res += handle_measure(elem, choices)
		elif elem.tag == f'{URI_MEI}sb':
			res += '/\n'

	return res


def handle_score(score: ET.Element, choices: list): # -> str
	res = ''

	# Possible direct children in <score>: <scoreDef>, <section>, <sb>
	for elem in score:
		if elem.tag == f'{URI_MEI}scoreDef':
			res += handle_scoreDef(elem, choices)
		elif elem.tag == f'{URI_MEI}section':
			res += handle_section(elem, choices)
		elif elem.tag == f'{URI_MEI}sb':
			res += '/\n'

	return res + '//'


def get_meterSigs(score: ET.Element): # -> list
	meterSigs = []
	elems_flat = list(score.iter()) # flat list of all elements in document order
	scoreDefs = score.findall('.//mei:scoreDef', ns)
	last_sd_ind = 0
	for scoreDef in scoreDefs:
		curr_ms = scoreDef.find('.//mei:meterSig', ns) # None if none found
		n = None
		for i in range(last_sd_ind, len(elems_flat)):
			elem = elems_flat[i]	
			if elem.get(XML_ID_KEY) == scoreDef.get(XML_ID_KEY):
				last_sd_ind = i
				next_measure = _find_first_elem_after(i, elems_flat, f'{URI_MEI}measure')
				n = next_measure.get('n')
				break
		meterSigs.append((n, curr_ms))

	return meterSigs


def _find_first_elem_after(ind: int, elems_flat: list, tag: str):
	return next(
		(e for e in elems_flat[ind + 1:] if e.tag == tag), None
	)


def get_meterinfo_and_diminution_str(score: ET.Element): # -> tuple
	mi = [] 
	dim = []

	meterSigs = get_meterSigs(score)
	measures = score.findall('.//mei:measure', ns)
	for i, (bar, ms) in enumerate(meterSigs):
		start_bar = int(bar)
		end_bar = int(meterSigs[i + 1][0]) - 1 if i < (len(meterSigs) - 1) else int(measures[-1].get('n'))
		meter = f'{ms.get('count')}/{ms.get('unit')}' if ms is not None else 'None'
		mi.append(f'{meter} ({start_bar}-{end_bar})')
		dim.append('1')

	return '; '.join(mi), '; '.join(dim)


if __name__ == "__main__":
	# - tbp events: TS event, RS event, rest event, MS event, barline event
	# - <choice>'s' <corr>/<sic> can contain: <scoreDef>, <measure>, <beam>, <tabGrp> 
	# - basic structure of the MEI, with <sb/> where they *may* appear
	#   <score>
	#       <scoreDef/>
	#	    <section>
	#           <scoreDef/>
	#           <measure>
	#               <beam>
	#                   <tabGrp>
	#                   <sb/>
	#               </beam>
	#               <tabGrp/>
	#               <sb/>
	#           </measure>
	#	        <sb/>
	#       </section>
	#       <sb/>
	#   <score>

	with open(os.path.join(inpath, infile), 'r', encoding='utf-8') as file:
		mei_str = file.read()

	# Handle namespaces
	ns = handle_namespaces(mei_str)
	URI_MEI = f'{{{ns['mei']}}}'
	URI_XML = f'{{{ns['xml']}}}'
	XML_ID_KEY = f'{URI_XML}id'

	# Get the tree, root (<mei>), and main MEI elements (<meiHead>, <score>)
	tree, root = parse_tree(mei_str)
	meiHead = root.find('mei:meiHead', ns)
	music = root.find('mei:music', ns)
	score = music.find('.//mei:score', ns)
	NOT_TYPE = score.find('.//mei:staffDef', ns).get('notationtype')

	# Collect all xml:ids
	xml_ids = [elem.attrib[XML_ID_KEY] for elem in root.iter() if XML_ID_KEY in elem.attrib]

	# Handle <choice>s
	check_xml = False
	check_elements = False
	# a. Split multi-measure <choice>s
	split_multi_measure_choice(root)
	if check_xml:
		write_xml(root, os.path.join(inpath, f'check-1{XML}'))
	# b. Implement <choice>s
	choices = {c.get(XML_ID_KEY): c for c in score.findall('.//mei:choice', ns)}
	choice_ids = choices.keys()
	implement_choice(root, choice_ids, TAG)
	if check_xml:
		write_xml(root, os.path.join(inpath, f'check-2{XML}'))
	if check_elements:
		print_all_elements(root, XML_ID_KEY)
		print_all_labelled_elements(root, XML_ID_KEY)

	# Make tbp encoding
	tbp_str = handle_score(score, choices)

	# Extract metadata
	work = meiHead.find('.//mei:workList', ns).find('.//mei:work', ns)
	composer = work.find('.//mei:composer', ns)
	title = work.find('.//mei:title', ns)
	tuning = score.find('.//mei:tuning', ns)
	author_str = composer.text if composer is not None else ''
	title_str = title.text if title is not None else ''
	source_str = ''
	tss_str = next((k for k, v in NOTATIONTYPES.items() if v == NOT_TYPE), None)
	tuning_str = get_tuning(tuning, ns) if tuning is not None else G
	meterinfo_str, diminution_str = get_meterinfo_and_diminution_str(score)

	# Serialise into JSON-formatted string and print
	res = [author_str, title_str, source_str, tss_str, tuning_str, meterinfo_str, diminution_str, tbp_str]
	print(json.dumps(res))


########################################################################

# Handle <sections>
# Possible direct children in <section>: <scoreDef>, <measure> (possibly within <choice>), <sb>
# TODO make arg_
def handle_section_OLD(section: ET.Element, choices: list, tbp: str): # -> str
	num_add_corr_events = 0

	for elem_sec in section:
		# 1. <section> element is <scoreDef> 
		if elem_sec.tag == f'{URI_MEI}scoreDef':
			ms_event = handle_scoreDef(elem_sec, choices)
			tbp += ms_event
		# 2. <section> element is <measure>
		if elem_sec.tag == f'{URI_MEI}measure':

#			print('M E A S U R E', elem_sec.get('n'))
#			measure = elem_sec
#			m_lbl = measure.get('label')
#			if m_lbl is not None and '<choice>' in m_lbl:
#-#				print('<measure> is', m_lbl)
#				pass
			m_event = handle_measure(elem_sec, choices, not_type)
			print()
			
			# Get <layer>
			layer = measure.find('.//mei:layer', ns)
			# Possible elements in <layer>: <beam>, <tabGrp>, <sb>
			for elem_lay in layer:
				# 2.1. <layer> element is <beam>
				if elem_lay.tag == f'{URI_MEI}beam':
					beam = elem_lay
					# Possible elements in <beam>: <tabGrp>, <sb>
					beam_events = handle_beam(beam, choices)
#					# Do not include closing '>.'
#					beam_events = beam_events[:-2]
					
					###############
#					b_lbl = beam.get('label')
##-#					print('B E A M', beam.get(XML_ID_KEY))
#					beam_events = ''
#					# Possible elements in <beam>: <tabGrp>, <sb>
#					beam_contents = list(beam)
#					for i, elem_beam in enumerate(beam_contents):
#						# 2.1.1. <beam> element is <tabGrp>
#						if elem_beam.tag == f'{URI_MEI}tabGrp':
#							tg_lbl = elem_beam.get('label')
##-#							print('T A B G R P  in  B E A M', elem_beam.get(XML_ID_KEY))
#							tabGrp_event = convert_tabGrp(elem_beam, not_type, i != len(beam_contents)-1)
##							tabGrp_event = convert_tabGrp(elem_beam, not_type, elem_beam != beam_contents[-1])
#							if tg_lbl is not None and '<choice>' in tg_lbl:
#								if 'first' in tg_lbl:
##-#									print('<tabGrp> is', tg_lbl)
#									choice_id = tg_lbl.strip().split()[-1]
#									c = choices[choice_id]
#									if TAG == 'corr':
#										alt = c.find('mei:sic', ns)									
#										# Possible elements in <sic>: <tabGrp>
#										tabGrp_event_footnote = ''
#										for j, item in enumerate(list(alt)):
#											tabGrp_event_footnote += convert_tabGrp(item, not_type, j != len(alt)-1)[:-2] # do not include closing '>.'
#										footnote = f'{{@\'{tabGrp_event_footnote}\' in source}}'
#										tabGrp_event = tabGrp_event[:-3] + footnote + tabGrp_event[-3:] # -3 is the length of '.>.'
##-#										print(tabGrp_event)
#								if 'following' in tg_lbl:
#									tabGrp_event = tabGrp_event[:-3] + '{@}' + tabGrp_event[-3:] # -3 is the length of '.>.'
#							beam_events += tabGrp_event
#					
#						# 2.1.2 <beam> element is <sb>
#						elif elem_beam.tag == f'{URI_MEI}sb':
#							beam_events += '\n/\n'
					###############
#					print('beam_events :', beam_events)
					tbp += beam_events

				# 2.2. <layer> element is <tabGrp>
				elif elem_lay.tag == f'{URI_MEI}tabGrp':
					tabGrp = elem_lay
					tg_lbl = tabGrp.get('label')
#					print('T A B G R P', tabGrp.get(XML_ID_KEY))
					tabGrp_event = convert_tabGrp(tabGrp, not_type, False) 
					if tg_lbl is not None and '<choice>' in tg_lbl:
						if 'first' in tg_lbl:
							print('i am the first', elem_lay.get(XML_ID_KEY))
#							print('<tabGrp> is', tg_lbl)
							choice_id = tg_lbl.strip().split()[-1]
							c = choices[choice_id]
							if TAG == 'corr':
								alt = c.find('mei:sic', ns)									
								# Possible elements in <sic>: <beam>, <tabGrp>
								tabGrp_event_footnote = ''
								for i, item in enumerate(list(alt)):
									if item.tag == f'{URI_MEI}beam':
										# Possible elements in <beam>: <tabGrp>, <sb>
										tabGrp_event_footnote += handle_beam(item, choices)
#										for j, itemitem in enumerate(list(item)):
#											tabGrp_event_footnote += handle_beam(itemitem, choices)	
									elif item.tag == f'{URI_MEI}tabGrp':
										tabGrp_event_footnote += convert_tabGrp(item, not_type, False)
										
								# Do not include closing '>.'	
								tabGrp_event_footnote = tabGrp_event_footnote[:-2]
#										tabGrp_event_footnote += convert_tabGrp(item, not_type, i != len(alt)-1)[:-2] # do not include closing '>.'
								footnote = f'{{@\'{tabGrp_event_footnote}\' in source}}'
								tabGrp_event = tabGrp_event[:-3] + footnote + tabGrp_event[-3:] # -3 is the length of '.>.'			
						if 'following' in tg_lbl:
							print('i follow', elem_lay.get(XML_ID_KEY))
							tabGrp_event = tabGrp_event[:-3] + '{@}' + tabGrp_event[-3:] # -3 is the length of '.>.'
#					print('tabGrp_event:', tabGrp_event)
					tbp += tabGrp_event 

				# 2.3. <layer> element is <sb>
				elif elem_lay.tag == f'{URI_MEI}sb':
#					sb = elem_lay
					tbp += '\n/\n'

#					# Footnote case					
#					if b_lbl is not None and '<choice>' in b_lbl:
#						print('<beam> is', b_lbl)
#						choice_id = b_lbl.strip().split()[-1]
#						c = choices[choice_id]
#						if TAG == 'corr':
#							alt = c.find('mei:sic', ns)
#							print(alt.find('mei:beam', ns))
#							ss
#							# If alt also has <beam>
#
#							# If alt has no <beam>
#
#							# Possible elements in <sic>: <tabGrp>
#							beam_contents = list(beam)
#							for i, tabGrp_beam in enumerate(alt):
#								add_beam = False if tabGrp_beam == beam_contents[-1] else True
#								tbp_event_footnote += convert_tabGrp(tabGrp_beam, not_type, add_beam)[:-2] # do not include closing '>.'
#									
#								# In the tbp, any corr events following the first corr event must 
#								# be followed by an empty footnote {@} (see MeiExport, getTabBar())
#								num_add_corr_events = (len(c.find('mei:corr', ns).findall('.//mei:tabGrp', ns))) - 1
#								print('=================', num_add_corr_events)
#
#					# If <beam> is in <choice>: make footnote here
#					# Else, make it inside loop below

#					beam_contents = list(beam)
#					# Possible elements in <beam>: <tabGrp> (possibly within <choice>), <sb>
#					for elem_beam in beam_contents:
#						# 2.1.1 <beam> element is <tabGrp>
#						if elem_beam.tag == f'{URI_MEI}tabGrp':
#							tabGrp_beamed = elem_beam
#							print('IS TABGRP IN BEAM', tabGrp_beamed.get(XML_ID_KEY))
#							add_beam = False if tabGrp_beamed == beam_contents[-1] else True
#
#							tbp_event = convert_tabGrp(tabGrp_beamed, not_type, add_beam)
#							print(tbp_event)
#							# If appropriate: make footnote
#							t_lbl = tabGrp_beamed.get('label')
#							if t_lbl is not None and '<choice>' in t_lbl: # or b_lbl is not None and '<choice>' in b_lbl
#								print('<tabGrp> is', t_lbl)
#								choice_id = t_lbl.strip().split()[-1]
#								c = choices[choice_id]
#								tbp_event_footnote = ''
#								if TAG == 'corr':
#									alt = c.find('mei:sic', ns)									
#									# Possible elements in <sic>: <tabGrp>
#									for item in alt:
#										tbp_event_footnote += convert_tabGrp(item, not_type, add_beam)[:-2] # do not include closing '>.'
#									
#									# In the tbp, any corr events following the first corr event must 
#									# be followed by an empty footnote {@} (see MeiExport, getTabBar())
#									num_add_corr_events = (len(c.find('mei:corr', ns).findall('.//mei:tabGrp', ns))) - 1
#									print('=================', num_add_corr_events)
#								elif TAG == 'sic':
#									alt = c.find('mei:corr', ns)
#								footnote = f'{{@\'{tbp_event_footnote}\' in source}}'
#
#								tbp_event = tbp_event[:-3] + footnote + tbp_event[-3:] # -3 is the length of '.>.'
#							print(tbp_event)
#							tbp += tbp_event

#						# 2.1.2 <beam> element is <sb>
#						if elem_beam.tag == f'{URI_MEI}sb':
#							tbp += '\n/\n'
				

			barline = measure.get('right')
			if barline != 'invis':
				tbp += f'{BARLINES[barline]}.'
			tbp += '\n'
		# 3. <section> element is <sb>
		if elem_sec.tag == f'{URI_MEI}sb':
#			sb = elem_sec
			tbp += '/\n'

	return tbp


def convert_beam_OLD(beam: ET.Element): # -> str
	beam_contents = list(beam)
	
	res = ''
	# Possible elements in <beam>: <tabGrp> (possibly within <choice>), <sb>
	for elem_beam in beam_contents:
		# <beam> element is <tabGrp>
		if elem_beam.tag == f'{URI_MEI}tabGrp':
			tabGrp_beamed = elem_beam
			print('IS TABGRP IN BEAM', tabGrp_beamed.get(XML_ID_KEY))
			add_beam = False if tabGrp_beamed == beam_contents[-1] else True

			res += convert_tabGrp(tabGrp_beamed, not_type, add_beam)
#			print(tbp_event)
			
#			# If appropriate: make footnote
#			t_lbl = tabGrp_beamed.get('label')
#			if t_lbl is not None and '<choice>' in t_lbl: # or b_lbl is not None and '<choice>' in b_lbl
#				print('<tabGrp> is', t_lbl)
#				choice_id = t_lbl.strip().split()[-1]
#				c = choices[choice_id]
#				tbp_event_footnote = ''
#				if TAG == 'corr':
#					alt = c.find('mei:sic', ns)									
#					# Possible elements in <sic>: <tabGrp>
#					for item in alt:
#						tbp_event_footnote += convert_tabGrp(item, not_type, add_beam)[:-2] # do not include closing '>.'
#								
#						# In the tbp, any corr events following the first corr event must 
#						# be followed by an empty footnote {@} (see MeiExport, getTabBar())
#						num_add_corr_events = (len(c.find('mei:corr', ns).findall('.//mei:tabGrp', ns))) - 1
#						print('=================', num_add_corr_events)
#				elif TAG == 'sic':
#					alt = c.find('mei:corr', ns)
#					footnote = f'{{@\'{tbp_event_footnote}\' in source}}'
#
#					tbp_event = tbp_event[:-3] + footnote + tbp_event[-3:] # -3 is the length of '.>.'
#				print(tbp_event)
			tbp += tbp_event

		# 2.2.2 <beam> element is <sb>
		if elem_beam.tag == f'{URI_MEI}sb':
			res += '\n/\n'

	return res


#			# Get any MensurationSign
#			measure_is_preceded_by_ms = any(item[0] == n for item in meterSigs)
#			print(measure_is_preceded_by_ms)
#			if measure_is_preceded_by_ms:
#				ms = next(item[1] for item in meterSigs if item[0] == n)
#				if ms != None:
#					ms_key = get_meterSig_key(ms)
#					mens_sig = f'{MENSURATION_SIGNS[ms_key]}.>.' 
#					
#					tbp += mens_sig

def handle_section_OLD(section: ET.Element, choices: list, tbp: str): # -> str
	num_add_corr_events = 0

	for elem_sec in section:
		# 1. <section> element is <scoreDef>
		if elem_sec.tag == f'{URI_MEI}scoreDef':
			scoreDef = elem_sec 
			meterSig = scoreDef.find('.//mei:meterSig', ns)
			ms_lbl = meterSig.get('label')
			print('M E T E R S I G')
			ms_event = '' if meterSig == None else convert_meterSig(meterSig)
			
			if ms_lbl is not None and '<choice>' in ms_lbl:
				print('<meterSig> is', ms_lbl)
				choice_id = ms_lbl.strip().split()[-1]
				c = choices[choice_id]
				if TAG == 'corr':
					alt = c.find('mei:sic', ns)									
					# Possible elements in <sic>: <meterSig>
					for item in alt:
						ms_event_footnote = convert_meterSig(item)[:-2] # do not include closing '>.'
					footnote = f'{{@\'{ms_event_footnote}\' in source}}'
					ms_event = ms_event[:-3] + footnote + ms_event[-3:] # -3 is the length of '.>.'				
			tbp += ms_event
		# 2. <section> element is <measure>
		if elem_sec.tag == f'{URI_MEI}measure':
			measure = elem_sec
			print('M E A S U R E', measure.get('n'))

			# If appropriate: make footnote
			m_lbl = measure.get('label')
			if m_lbl is not None and '<choice>' in m_lbl:
				print('<measure> is', m_lbl)

			# Possible elements in <layer>: <tabGrp>, <beam> (both possibly within <choice>), <sb>
			layer = measure.find('.//mei:layer', ns)
			for elem_lay in layer:
				# 2.2. <layer> element is <beam>
				if elem_lay.tag == f'{URI_MEI}beam':
					beam = elem_lay
					b_lbl = beam.get('label')
					print('B E A M', beam.get(XML_ID_KEY))
					print(b_lbl)
					beam_events = ''

					# Normal case
					# Possible elements in <beam>: <tabGrp>, <sb>
					for item in list(beam):
						print(item)
						if item.tag == f'{URI_MEI}tabGrp':
							beam_events += convert_tabGrp(item, not_type, item != list(beam)[-1])
						elif item.tag == f'{URI_MEI}sb':
							beam_events += '\n/\n'
					print(beam_events)

					# Footnote case					
					if b_lbl is not None and '<choice>' in b_lbl:
						print('<beam> is', b_lbl)
						choice_id = b_lbl.strip().split()[-1]
						c = choices[choice_id]
						if TAG == 'corr':
							alt = c.find('mei:sic', ns)
							print(alt.find('mei:beam', ns))
							ss
							# If alt also has <beam>

							# If alt has no <beam>

							# Possible elements in <sic>: <tabGrp>
							beam_contents = list(beam)
							for i, tabGrp_beam in enumerate(alt):
								add_beam = False if tabGrp_beam == beam_contents[-1] else True
								tbp_event_footnote += convert_tabGrp(tabGrp_beam, not_type, add_beam)[:-2] # do not include closing '>.'
									
								# In the tbp, any corr events following the first corr event must 
								# be followed by an empty footnote {@} (see MeiExport, getTabBar())
								num_add_corr_events = (len(c.find('mei:corr', ns).findall('.//mei:tabGrp', ns))) - 1
								print('=================', num_add_corr_events)

					# If <beam> is in <choice>: make footnote here
					# Else, make it inside loop below

					beam_contents = list(beam)
					# Possible elements in <beam>: <tabGrp> (possibly within <choice>), <sb>
					for elem_beam in beam_contents:
						# 2.2.1 <beam> element is <tabGrp>
						if elem_beam.tag == f'{URI_MEI}tabGrp':
							tabGrp_beamed = elem_beam
							print('IS TABGRP IN BEAM', tabGrp_beamed.get(XML_ID_KEY))
							add_beam = False if tabGrp_beamed == beam_contents[-1] else True

							tbp_event = convert_tabGrp(tabGrp_beamed, not_type, add_beam)
							print(tbp_event)
							# If appropriate: make footnote
							t_lbl = tabGrp_beamed.get('label')
							if t_lbl is not None and '<choice>' in t_lbl: # or b_lbl is not None and '<choice>' in b_lbl
								print('<tabGrp> is', t_lbl)
								choice_id = t_lbl.strip().split()[-1]
								c = choices[choice_id]
								tbp_event_footnote = ''
								if TAG == 'corr':
									alt = c.find('mei:sic', ns)									
									# Possible elements in <sic>: <tabGrp>
									for item in alt:
										tbp_event_footnote += convert_tabGrp(item, not_type, add_beam)[:-2] # do not include closing '>.'
									
									# In the tbp, any corr events following the first corr event must 
									# be followed by an empty footnote {@} (see MeiExport, getTabBar())
									num_add_corr_events = (len(c.find('mei:corr', ns).findall('.//mei:tabGrp', ns))) - 1
									print('=================', num_add_corr_events)
								elif TAG == 'sic':
									alt = c.find('mei:corr', ns)
								footnote = f'{{@\'{tbp_event_footnote}\' in source}}'

								tbp_event = tbp_event[:-3] + footnote + tbp_event[-3:] # -3 is the length of '.>.'
							print(tbp_event)
							tbp += tbp_event

						# 2.2.2 <beam> element is <sb>
						if elem_beam.tag == f'{URI_MEI}sb':
							tbp += '\n/\n'
				
				# 2.1. <layer> element is <tabGrp>
				elif elem_lay.tag == f'{URI_MEI}tabGrp':
					print('IS TABGRP', elem_lay.get(XML_ID_KEY))
					tabGrp = elem_lay
					tbp += convert_tabGrp(tabGrp, not_type, False)

					# If appropriate: make footnote
					t_lbl = tabGrp.get('label')
					print(t_lbl)
#					if t_lbl is not None and '<choice>' in t_lbl:
#						print('tabGrp> is', t_lbl)
#						choice_id = t_lbl.strip().split()[-1]
#						print(choice_id)

				# 2.3. <layer> element is <sb>
				elif elem_lay.tag == f'{URI_MEI}sb':
#					sb = elem_lay
					tbp += '\n/\n'
			barline = measure.get('right')
			if barline != 'invis':
				tbp += f'{BARLINES[barline]}.'
			tbp += '\n'
		# 3. <section> element is <sb>
		if elem_sec.tag == f'{URI_MEI}sb':
#			sb = elem_sec
			tbp += '/\n'

	return tbp	


def get_meterSig_key(meterSig: str): # -> str
	count = meterSig.get('count')
	unit = meterSig.get('unit')
	form = meterSig.get('form')
	sym = meterSig.get('sym')

	# @count, @unit
	ms_key = f'{count}:{unit}'
	# @form='num'
	if form is not None:
		ms_key += f'_{form}'
	# @sym='cut', @sym='common'
	if sym is not None:
		ms_key += f'_{sym}'

	return ms_key	


def find_parent(root, child):
	for parent in root.iter():
		for elem in parent:
			if elem is child:
				return parent

	return None


def implement_choice_inflexible(root: ET.Element, choice_ids: list, tag: str):
    replacements = []

    # First pass: collect changes
    for elem in root.iter():
        for i, child in enumerate(list(elem)):
            if child.tag == f'{URI_MEI}choice':
                choice_id = child.get(XML_ID_KEY)
                if choice_id in choice_ids:
                    choice_elem = child.find(f'mei:{tag}', ns)
                    if choice_elem is not None:
                        new_elems = []
                        tabGrp_cnt = 1
                        num_tabGrps = len(choice_elem.findall('.//mei:tabGrp', ns))

                        for sub in list(choice_elem):
                            sub_copy = copy.deepcopy(sub)
                            
                            if sub_copy.tag == f'{URI_MEI}beam':
                            	for item in sub_copy:
                            		if item.tag == f'{URI_MEI}tabGrp':
                            			pos = 'first' if tabGrp_cnt == 1 else 'following'
                            			item.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')		
                            			tabGrp_cnt += 1		
                            if sub_copy.tag == f'{URI_MEI}tabGrp':
                                pos = 'first' if tabGrp_cnt == 1 else 'following'
                                sub_copy.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
                                tabGrp_cnt += 1
                            if sub_copy.tag == f'{URI_MEI}scoreDef':
                                sub_copy.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')

                            new_elems.append(sub_copy)

                        replacements.append((elem, i, child, new_elems))

    # Second pass: process in reverse to avoid index shifting issues
    for elem, i, choice_elem, new_elems in reversed(replacements):
        for offset, new_elem in enumerate(new_elems):
            elem.insert(i + offset, new_elem)
        elem.remove(choice_elem)


def implement_choice_SHIT(root: ET.Element, choice_ids: list, tag: str):
	for elem in root.iter():
		for i, child in enumerate(list(elem)):
			if child.tag == f'{URI_MEI}choice':
				choice_id = child.get(XML_ID_KEY)
				if choice_id in choice_ids:
					choice_elem = child.find(f'mei:{tag}', ns) # <corr> or <sic>
					if choice_elem is not None:
						choice_elem_copy = copy.deepcopy(choice_elem)

						# Count all tabGrps
						num_tabGrps = len(choice_elem_copy.findall('.//mei:tabGrp', ns))
						tabGrp_cnt = 1

						# Process the copied subtree with beam tracking
						def label_elements(elem, inside_beam=False):
							nonlocal tabGrp_cnt
							if elem.tag == f'{URI_MEI}beam':
								inside_beam = True
							if elem.tag == f'{URI_MEI}tabGrp':
								b = ', beamed, ' if inside_beam else ' '
								pos = 'first' if tabGrp_cnt == 1 else 'following'
								elem.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
#								elem.set('label', f'<tabGrp> {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
								tabGrp_cnt += 1
#							if elem.tag == f'{URI_MEI}meterSig':
#								elem.set('label', f'<meterSig> from <choice> with @xml:id {choice_id}')
							if elem.tag == f'{URI_MEI}scoreDef':
								elem.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')
							for child_elem in elem:
								label_elements(child_elem, inside_beam)

						label_elements(choice_elem_copy)

						# Insert top-level children of <corr> or <sic> copy
						for offset, e in enumerate(list(choice_elem_copy)):
							elem.insert(i + offset, e)

						# Remove the original <choice> element
						elem.remove(child)


def implement_choice_DOESNT_WORK(root: ET.Element, choice_ids: list, tag: str): # -> None
	for elem in root.iter():
		for i, child in enumerate(list(elem)): # use list() to safely modify
			if child.tag == f'{URI_MEI}choice':
				choice_id = child.get(XML_ID_KEY)
				if choice_id in choice_ids:
					# choice_elem is <corr> or <sic>
					choice_elem = child.find(f'mei:{tag}', ns)
					if choice_elem is not None:
						print('C H O I C E')
						# Insert all children at the position of the <choice> element
						choice_elem_contents = list(choice_elem) # use list() to safely modify
						num_tabGrps = len(choice_elem.findall('.//mei:tabGrp', ns))
						tabGrp_cnt = 1
						# For each child
#						num_beamed_tabGrps = 0
#						for j, choice_elem_child in enumerate(islice(choice_elem.iter(), 1, None)): # iter() starts w/ choice_elem itself
						for j, choice_elem_child in enumerate(choice_elem_contents):
							choice_elem_child_copy = copy.deepcopy(choice_elem_child)
							print(choice_elem_child_copy.tag)

#							if choice_elem_child_copy.tag == f'{URI_MEI}beam':
#								num_beamed_tabGrps = len(choice_elem_child_copy.findall('.//mei:tabGrp', ns))
			
							if choice_elem_child_copy.tag == f'{URI_MEI}tabGrp':
#								print("BLA")
#								print(num_beamed_tabGrps)
#								b = ''
#								if num_beamed_tabGrps > 0:
##									print('jepperdepep')
#									b = ', beamed, '
#									num_beamed_tabGrps -= 1
								pos = 'first' if tabGrp_cnt == 1 else 'following'
								choice_elem_child_copy.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
#								choice_elem_child_copy.set('label', f'tabGrp {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
								tabGrp_cnt += 1	
#							# Add label to each <tabGrp> and each <meterSig>
#							for e in choice_elem_child_copy.iter():
#								if e.tag == f'{URI_MEI}tabGrp':
#									b = ''
#									b = ', beamed, '
#									e.set('label', f'tabGrp {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
#									tabGrp_cnt += 1
#								if e.tag == f'{URI_MEI}meterSig':
#									e.set('label', f'1/1 from <choice> with @xml:id {choice_id}')

							if choice_elem_child_copy.tag == f'{URI_MEI}meterSig':
								choice_elem_child_copy.set('label', f'1/1 from <choice> with @xml:id {choice_id}')
							if choice_elem_child_copy.tag == f'{URI_MEI}scoreDef':
								choice_elem_child_copy.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')
							
							elem.insert(i + j, choice_elem_child_copy)
						# Remove the original <choice> element
						elem.remove(child)




