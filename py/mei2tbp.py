import copy
import json
import os
import sys
import xml.etree.ElementTree as ET

# Ensure that Python can find .py files in utils/py/ regardless of where the script
# is run from by adding the path holding the code (<lib_path>) to sys.path
# __file__ 					= <lib_path>/formats/py/mei2tbp.py
# os.path.dirname(__file__) = <lib_path>/formats/py/
# '../../' 					= up two levels, i.e., <lib_path>
lib_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../utils'))
if lib_path not in sys.path:
	sys.path.insert(0, lib_path)

from py.constants import *
from py.utils import (get_tuning, add_unique_id, handle_namespaces, parse_tree, get_main_MEI_elements,
					  collect_xml_ids, find_first_elem_after, write_xml, print_all_elements, 
					  print_all_labelled_elements)

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
TAG = 'corr' # TODO make argument
#TAG = 'sic'
URI_MEI = None
URI_XML = None
XML_ID_KEY = None
NOT_TYPE = None
xml_ids = None

_, inpath, infile = sys.argv


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


def get_meterinfo_and_diminution_str(score: ET.Element): # -> tuple
	mi = [] 
	dim = []

	meterSigs = _get_meterSigs(score)
	measures = score.findall('.//mei:measure', ns)
	for i, (bar, ms) in enumerate(meterSigs):
		start_bar = int(bar)
		end_bar = int(meterSigs[i + 1][0]) - 1 if i < (len(meterSigs) - 1) else int(measures[-1].get('n'))
		meter = f'{ms.get('count')}/{ms.get('unit')}' if ms is not None else 'None'
		mi.append(f'{meter} ({start_bar}-{end_bar})')
		dim.append('1')

	return '; '.join(mi), '; '.join(dim)


def _get_meterSigs(score: ET.Element): # -> list
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
				next_measure = find_first_elem_after(i, elems_flat, f'{URI_MEI}measure')
				n = next_measure.get('n')
				break
		meterSigs.append((n, curr_ms))

	return meterSigs


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
	meiHead, music = get_main_MEI_elements(root, ns)
#	meiHead = root.find('mei:meiHead', ns)
#	music = root.find('mei:music', ns)
	score = music.find('.//mei:score', ns)
	NOT_TYPE = score.find('.//mei:staffDef', ns).get('notationtype')

	# Collect all xml:ids
	xml_ids = collect_xml_ids(root, XML_ID_KEY)
#	xml_ids = [elem.attrib[XML_ID_KEY] for elem in root.iter() if XML_ID_KEY in elem.attrib]

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
	# TODO to method
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
