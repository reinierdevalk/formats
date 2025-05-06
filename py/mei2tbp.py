import copy
import os
import xml.etree.ElementTree as ET
from io import StringIO
from itertools import islice
from sys import argv
script, inpath, infile = argv

NUM_COURSES = 6

# TODO use final fields from parser_vals.py
FLT = 'FLT'
ILT = 'ILT'
SLT = 'SLT'
GLT = 'GLT'
NOTATIONTYPES = {FLT: 'tab.lute.french',
				 ILT: 'tab.lute.italian',
				 SLT: 'tab.lute.spanish',
				 GLT: 'tab.lute.german'
				}
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



def handle_namespaces(xml_contents: str): # -> dict
	# There is only one namespace, whose key is an empty string -- replace the  
	# key with something meaningful ('mei'). See
	# https://stackoverflow.com/questions/42320779/get-the-namespaces-from-xml-with-python-elementtree/42372404#42372404
	# To avoid an 'ns0' prefix before each tag, register the namespace as an empty string. See
	# https://stackoverflow.com/questions/8983041/saving-xml-files-using-elementtree
	#
	# StringIO treats a string as a file-like object that can be iterated over
	ns = dict([node for _, node in ET.iterparse(StringIO(xml_contents), events=['start-ns'])])
#	ns = dict([node for _, node in ET.iterparse(path, events=['start-ns'])])
	ns['mei'] = ns.pop('')
	ET.register_namespace('', ns['mei'])
	ns['xml'] = 'http://www.w3.org/XML/1998/namespace'

	return ns


def parse_tree(xml_contents: str): # -> Tuple
	"""
	Basic structure of <mei>:
	
	<mei> 
	  <meiHead/>
	  <music>
	    ...
	    <score>
	      <scoreDef/>
	      <section/>
	    </score>
	  </music>
	</mei>   
	"""
	tree = ET.ElementTree(ET.fromstring(xml_contents))
#	tree = ET.parse(path)
	root = tree.getroot()

	return (tree, root)


def implement_choice_inflexible(root: ET.Element, choice_ids: list, tag: str):
    replacements = []

    # First pass: collect changes
    for elem in root.iter():
        for i, child in enumerate(list(elem)):
            if child.tag == f'{uri_mei}choice':
                choice_id = child.get(xml_id_key)
                if choice_id in choice_ids:
                    choice_elem = child.find(f'mei:{tag}', ns)
                    if choice_elem is not None:
                        new_elems = []
                        tabGrp_cnt = 1
                        num_tabGrps = len(choice_elem.findall('.//mei:tabGrp', ns))

                        for sub in list(choice_elem):
                            sub_copy = copy.deepcopy(sub)
                            
                            if sub_copy.tag == f'{uri_mei}beam':
                            	for item in sub_copy:
                            		if item.tag == f'{uri_mei}tabGrp':
                            			pos = 'first' if tabGrp_cnt == 1 else 'following'
                            			item.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')		
                            			tabGrp_cnt += 1		
                            if sub_copy.tag == f'{uri_mei}tabGrp':
                                pos = 'first' if tabGrp_cnt == 1 else 'following'
                                sub_copy.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
                                tabGrp_cnt += 1
                            if sub_copy.tag == f'{uri_mei}scoreDef':
                                sub_copy.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')

                            new_elems.append(sub_copy)

                        replacements.append((elem, i, child, new_elems))

    # Second pass: process in reverse to avoid index shifting issues
    for elem, i, choice_elem, new_elems in reversed(replacements):
        for offset, new_elem in enumerate(new_elems):
            elem.insert(i + offset, new_elem)
        elem.remove(choice_elem)


def implement_choice(root: ET.Element, choice_ids: list, tag: str):
    replacements = []

    for elem in root.iter():
        for i, child in enumerate(list(elem)):
            if child.tag == f'{uri_mei}choice':
                choice_id = child.get(xml_id_key)
                if choice_id in choice_ids:
                    choice_elem = child.find(f'mei:{tag}', ns)
                    if choice_elem is not None:
                        # Deepcopy to avoid modifying original
                        choice_elem_copy = copy.deepcopy(choice_elem)

                        # Count total <tabGrp> for label logic
                        num_tabGrps = len(choice_elem_copy.findall('.//mei:tabGrp', ns))
                        tabGrp_cnt = 1

                        # Recursively walk all elements and annotate
                        for sub in choice_elem_copy.iter():
                            if sub.tag == f'{uri_mei}tabGrp':
                                pos = 'first' if tabGrp_cnt == 1 else 'following'
                                sub.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
                                tabGrp_cnt += 1
                            elif sub.tag == f'{uri_mei}scoreDef':
                                sub.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')

                        # We only want to insert the top-level children of <corr>/<sic>
                        new_elems = list(choice_elem_copy)

                        replacements.append((elem, i, child, new_elems))

    # Insert and remove <choice> (in reverse to prevent index shifting)
    for elem, i, choice_elem, new_elems in reversed(replacements):
        for offset, new_elem in enumerate(new_elems):
            elem.insert(i + offset, new_elem)
        elem.remove(choice_elem)


def insert_footnote_in_event(choices: list, event: str, lbl: str, 
							 is_single_tabGrp_in_beam: bool, is_ts_event: bool): # -> str

	if (is_ts_event and 'first' in lbl) or not is_ts_event:
		choice_id = lbl.strip().split()[-1]
		c = choices[choice_id]
		alt_events = _handle_alt(c, choices, is_ts_event)
		# A single <tabGrp> in a <beam> is wrapped directly in <choice> (and not first in 
		# <beam>), meaning that the MS will not yet be beamed
		if is_ts_event and is_single_tabGrp_in_beam:
			ms = alt_events[:alt_events.index('.')]
			alt_events = alt_events.replace(ms, f'{ms}-')
		# Do not include closing '>.'		
		alt_events = alt_events[:-2]
		footnote = f'{{@\'{alt_events}\' {'in source' if TAG == 'corr' else 'corrected'}}}'
	if is_ts_event and 'following' in lbl:
		footnote = '{@}'
	event = event[:-3] + footnote + event[-3:] # -3 is the length of '.>.'

	return event


def _handle_alt(choice: ET.Element, choices: list, is_ts_event: bool): # -> str
	alt_events = ''

	alt = choice.find(f'mei:{'sic' if TAG == 'corr' else 'corr'}', ns)
	# TS event
	if is_ts_event:
		# Possible direct children: <beam>, <tabGrp>, <sb>
		for j, item in enumerate(list(alt)):
			if item.tag == f'{uri_mei}beam':
				alt_events += handle_beam(item, choices)
			if item.tag == f'{uri_mei}tabGrp':
				alt_events += convert_tabGrp(item, False)#j != len(alt)-1) 
			elif item.tag == f'{uri_mei}sb':
				alt_events += '\n/\n'
	# MS event
	else:
		meterSig_alt = alt.find('.//mei:meterSig', ns)
		alt_events = convert_meterSig(meterSig_alt)

	return alt_events


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
	rhythm_symbol = ''
	if tabGrp.find('mei:tabDurSym', ns) is not None:
		dur = tabGrp.get('dur')
		dot = '*' if tabGrp.get('dots') is not None else ''
		beam = '-' if is_beamed else ''
		rhythm_symbol = f'{DURATIONS[dur]}{dot}{beam}.'  

	# Determine TabSymbols
	event_list = [None, None, None, None, None, None] # first element is lowest-sounding course
	for elem in tabGrp:
		if elem.tag == f'{uri_mei}note':
			fret = elem.get('tab.fret')
			course = elem.get('tab.course')
			if NOT_TYPE == NOTATIONTYPES[FLT]:
				tab_symbol = f'{"abcdefghikl"[int(fret)]}{course}.'
			elif NOT_TYPE == NOTATIONTYPES[GLT]:
				tab_symbol = f'{NEWSIDLER[int(course)-1][int(fret)]}.'
			else:
				tab_symbol = f'{fret}{course}.'
			event_list[NUM_COURSES - int(course)] = tab_symbol
	
	# Make event		
	tab_symbols = ''
	for tab_symbol in event_list:
		if tab_symbol is not None:
			tab_symbols += tab_symbol

	return f'{rhythm_symbol}{tab_symbols}>.'


def handle_beam(beam: ET.Element, choices: list): # -> str
	b_events = ''

	# Possible direct children in <beam>: <tabGrp>, <sb>
	b_contents = list(beam)
	for i, elem in enumerate(b_contents):
		if elem.tag == f'{uri_mei}tabGrp':
			lbl = elem.get('label')
			is_beamed = i != len(b_contents) - 1
			ts_event = convert_tabGrp(elem, is_beamed)
			if lbl is not None and '<choice>' in lbl:
				is_single_tg_in_beam = True if ('(1/1)' in lbl and is_beamed) else False
				ts_event = insert_footnote_in_event(choices, ts_event, lbl, 
													is_single_tg_in_beam, True)
			b_events += ts_event
		elif elem.tag == f'{uri_mei}sb':
			b_events += '\n/\n'

	return b_events


def handle_measure(measure: ET.Element, choices: list): # -> str
	m_events = ''

	# Possible direct children in <layer>: <beam>, <tabGrp>, <sb>
	layer = measure.find('.//mei:layer', ns)
	for elem in layer:
		if elem.tag == f'{uri_mei}beam':
			m_events += handle_beam(elem, choices)
		elif elem.tag == f'{uri_mei}tabGrp':
			lbl = elem.get('label')
			ts_event = convert_tabGrp(elem, False)
			if lbl is not None and '<choice>' in lbl:
				ts_event = insert_footnote_in_event(choices, ts_event, lbl, False, True)
			m_events += ts_event
		elif elem.tag == f'{uri_mei}sb':
			m_events += '\n/\n'
	barline = measure.get('right')
	if barline != 'invis':
		m_events += f'{BARLINES[barline]}.'
	m_events += '\n'

	return m_events


def handle_scoreDef(scoreDef: ET.Element, choices: list): # -> str
	ms_event = ''

	meterSig = scoreDef.find('.//mei:meterSig', ns)
	if meterSig is not None:
		ms_event = convert_meterSig(meterSig) 
		lbl = scoreDef.get('label') # lbl is on <scoreDef>: <choice> cannot contain <scoreDef> children 
		if lbl is not None and '<choice>' in lbl:
			ms_event = insert_footnote_in_event(choices, ms_event, lbl, False, False)

	return ms_event


def handle_section(section: ET.Element, choices: list): # -> str
	res = ''

	# Possible direct children in <section>: <scoreDef>, <measure>, <sb>
	for elem in section:
		if elem.tag == f'{uri_mei}scoreDef':
			res += handle_scoreDef(elem, choices)
		if elem.tag == f'{uri_mei}measure':
			res += handle_measure(elem, choices)
		if elem.tag == f'{uri_mei}sb':
			res += '/\n'

	return res


def handle_score(score: ET.Element, choices: list): # -> str
	res = ''

	# Possible direct children in <score>: <scoreDef>, <section>, <sb>
	for elem in score:
		if elem.tag == f'{uri_mei}scoreDef':
			ms_event = handle_scoreDef(elem, choices)
			res += ms_event
		if elem.tag == f'{uri_mei}section':
			s = handle_section(elem, choices)
			res += s
		if elem.tag == f'{uri_mei}sb':
			res += '/\n'

	return res


#if __name__ == "__main__":
with open(os.path.join(inpath, infile), 'r', encoding='utf-8') as file:
	mei_str = file.read()

# Handle namespaces
ns = handle_namespaces(mei_str)
uri_mei = f'{{{ns['mei']}}}'
uri_xml = f'{{{ns['xml']}}}'
xml_id_key = f'{uri_xml}id'

# Get the tree, root (<mei>), and main MEI elements (<meiHead>, <score>)
tree, root = parse_tree(mei_str)
meiHead = root.find('mei:meiHead', ns)
music = root.find('mei:music', ns)
score = music.find('.//mei:score', ns)

scoreDefs = score.findall('.//mei:scoreDef', ns)
NOT_TYPE = scoreDefs[0].find('.//mei:staffDef', ns).get('notationtype')

#meterSigs = []
#elems_flat = list(score.iter()) # flat list of all elements in document order
#last_sd_ind = 0
#for scoreDef in scoreDefs:
#	curr_ms = scoreDef.find('.//mei:meterSig', ns) # None if none found
#	n = None
#	for i in range(last_sd_ind, len(elems_flat)):
#		elem = elems_flat[i]	
#		if elem.get(xml_id_key) == scoreDef.get(xml_id_key):
#			last_sd_ind = i
#			next_measure = _find_first_elem_after(i, elems_flat, f'{uri_mei}measure')
#			n = next_measure.get('n')
#			break
#	meterSigs.append((n, curr_ms))

#choices = []
#for choice in root.findall('.//mei:choice', ns):
#	choices.append(choice.get(xml_id_key))

#choicess = score.findall('.//mei:choice', ns)
#corrs = {c.find('mei:corr', ns).get(xml_id_key): c.find('mei:corr', ns) for c in choicess}
#sics = {c.find('mei:sic', ns).get(xml_id_key): c.find('mei:sic', ns) for c in choicess}
#corr_sic_ids = [c.find('mei:corr', ns) for c in choicess]
#choice_idss = [c.get(xml_id_key) for c in choicess]

choices = {c.get(xml_id_key): c for c in score.findall('.//mei:choice', ns)}
choice_ids = choices.keys()

TAG = 'corr'
#TAG = 'sic'
implement_choice(root, choice_ids, TAG)


#for elem in root.iter():
#	print(elem, elem.get(xml_id_key))

#for elem in root.iter():
#	if elem.get('label') != None:
#		print(elem.get(xml_id_key), elem.get('label'))




#sections = score.findall('mei:section', ns)

tbp_enc = handle_score(score, choices)
print(tbp_enc)


# tbp events: TS event, RS event, rest event, MS event, barline event
#
# <choice> can contain: <scoreDef>, <measure>, <beam>, <tabGrp> 
#
# <score>
#     <scoreDef/>
#	  <section>
#         <scoreDef/>
#         <measure>
#             <beam>
#                 <tabGrp>
#                 <sb/>
#             </beam>
#             <tabGrp/>
#             <sb/>
#         </measure>
#	      <sb/>
#     </section>
#     <sb/>
# <score>


########################################################################

# Handle <sections>
# Possible direct children in <section>: <scoreDef>, <measure> (possibly within <choice>), <sb>
# TODO make arg_
def handle_section_OLD(section: ET.Element, choices: list, tbp: str): # -> str
	num_add_corr_events = 0

	for elem_sec in section:
		# 1. <section> element is <scoreDef> 
		if elem_sec.tag == f'{uri_mei}scoreDef':
			ms_event = handle_scoreDef(elem_sec, choices)
			tbp += ms_event
		# 2. <section> element is <measure>
		if elem_sec.tag == f'{uri_mei}measure':

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
				if elem_lay.tag == f'{uri_mei}beam':
					beam = elem_lay
					# Possible elements in <beam>: <tabGrp>, <sb>
					beam_events = handle_beam(beam, choices)
#					# Do not include closing '>.'
#					beam_events = beam_events[:-2]
					
					###############
#					b_lbl = beam.get('label')
##-#					print('B E A M', beam.get(xml_id_key))
#					beam_events = ''
#					# Possible elements in <beam>: <tabGrp>, <sb>
#					beam_contents = list(beam)
#					for i, elem_beam in enumerate(beam_contents):
#						# 2.1.1. <beam> element is <tabGrp>
#						if elem_beam.tag == f'{uri_mei}tabGrp':
#							tg_lbl = elem_beam.get('label')
##-#							print('T A B G R P  in  B E A M', elem_beam.get(xml_id_key))
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
#						elif elem_beam.tag == f'{uri_mei}sb':
#							beam_events += '\n/\n'
					###############
#					print('beam_events :', beam_events)
					tbp += beam_events

				# 2.2. <layer> element is <tabGrp>
				elif elem_lay.tag == f'{uri_mei}tabGrp':
					tabGrp = elem_lay
					tg_lbl = tabGrp.get('label')
#					print('T A B G R P', tabGrp.get(xml_id_key))
					tabGrp_event = convert_tabGrp(tabGrp, not_type, False) 
					if tg_lbl is not None and '<choice>' in tg_lbl:
						if 'first' in tg_lbl:
							print('i am the first', elem_lay.get(xml_id_key))
#							print('<tabGrp> is', tg_lbl)
							choice_id = tg_lbl.strip().split()[-1]
							c = choices[choice_id]
							if TAG == 'corr':
								alt = c.find('mei:sic', ns)									
								# Possible elements in <sic>: <beam>, <tabGrp>
								tabGrp_event_footnote = ''
								for i, item in enumerate(list(alt)):
									if item.tag == f'{uri_mei}beam':
										# Possible elements in <beam>: <tabGrp>, <sb>
										tabGrp_event_footnote += handle_beam(item, choices)
#										for j, itemitem in enumerate(list(item)):
#											tabGrp_event_footnote += handle_beam(itemitem, choices)	
									elif item.tag == f'{uri_mei}tabGrp':
										tabGrp_event_footnote += convert_tabGrp(item, not_type, False)
										
								# Do not include closing '>.'	
								tabGrp_event_footnote = tabGrp_event_footnote[:-2]
#										tabGrp_event_footnote += convert_tabGrp(item, not_type, i != len(alt)-1)[:-2] # do not include closing '>.'
								footnote = f'{{@\'{tabGrp_event_footnote}\' in source}}'
								tabGrp_event = tabGrp_event[:-3] + footnote + tabGrp_event[-3:] # -3 is the length of '.>.'			
						if 'following' in tg_lbl:
							print('i follow', elem_lay.get(xml_id_key))
							tabGrp_event = tabGrp_event[:-3] + '{@}' + tabGrp_event[-3:] # -3 is the length of '.>.'
#					print('tabGrp_event:', tabGrp_event)
					tbp += tabGrp_event 

				# 2.3. <layer> element is <sb>
				elif elem_lay.tag == f'{uri_mei}sb':
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
#						if elem_beam.tag == f'{uri_mei}tabGrp':
#							tabGrp_beamed = elem_beam
#							print('IS TABGRP IN BEAM', tabGrp_beamed.get(xml_id_key))
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
#						if elem_beam.tag == f'{uri_mei}sb':
#							tbp += '\n/\n'
				

			barline = measure.get('right')
			if barline != 'invis':
				tbp += f'{BARLINES[barline]}.'
			tbp += '\n'
		# 3. <section> element is <sb>
		if elem_sec.tag == f'{uri_mei}sb':
#			sb = elem_sec
			tbp += '/\n'

	return tbp


def convert_beam_OLD(beam: ET.Element): # -> str
	beam_contents = list(beam)
	
	res = ''
	# Possible elements in <beam>: <tabGrp> (possibly within <choice>), <sb>
	for elem_beam in beam_contents:
		# <beam> element is <tabGrp>
		if elem_beam.tag == f'{uri_mei}tabGrp':
			tabGrp_beamed = elem_beam
			print('IS TABGRP IN BEAM', tabGrp_beamed.get(xml_id_key))
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
		if elem_beam.tag == f'{uri_mei}sb':
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
		if elem_sec.tag == f'{uri_mei}scoreDef':
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
		if elem_sec.tag == f'{uri_mei}measure':
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
				if elem_lay.tag == f'{uri_mei}beam':
					beam = elem_lay
					b_lbl = beam.get('label')
					print('B E A M', beam.get(xml_id_key))
					print(b_lbl)
					beam_events = ''

					# Normal case
					# Possible elements in <beam>: <tabGrp>, <sb>
					for item in list(beam):
						print(item)
						if item.tag == f'{uri_mei}tabGrp':
							beam_events += convert_tabGrp(item, not_type, item != list(beam)[-1])
						elif item.tag == f'{uri_mei}sb':
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
						if elem_beam.tag == f'{uri_mei}tabGrp':
							tabGrp_beamed = elem_beam
							print('IS TABGRP IN BEAM', tabGrp_beamed.get(xml_id_key))
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
						if elem_beam.tag == f'{uri_mei}sb':
							tbp += '\n/\n'
				
				# 2.1. <layer> element is <tabGrp>
				elif elem_lay.tag == f'{uri_mei}tabGrp':
					print('IS TABGRP', elem_lay.get(xml_id_key))
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
				elif elem_lay.tag == f'{uri_mei}sb':
#					sb = elem_lay
					tbp += '\n/\n'
			barline = measure.get('right')
			if barline != 'invis':
				tbp += f'{BARLINES[barline]}.'
			tbp += '\n'
		# 3. <section> element is <sb>
		if elem_sec.tag == f'{uri_mei}sb':
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


def implement_choice_SHIT(root: ET.Element, choice_ids: list, tag: str):
	for elem in root.iter():
		for i, child in enumerate(list(elem)):
			if child.tag == f'{uri_mei}choice':
				choice_id = child.get(xml_id_key)
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
							if elem.tag == f'{uri_mei}beam':
								inside_beam = True
							if elem.tag == f'{uri_mei}tabGrp':
								b = ', beamed, ' if inside_beam else ' '
								pos = 'first' if tabGrp_cnt == 1 else 'following'
								elem.set('label', f'{pos} ({tabGrp_cnt}/{num_tabGrps}) <tabGrp> from <choice> with @xml:id {choice_id}')
#								elem.set('label', f'<tabGrp> {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
								tabGrp_cnt += 1
#							if elem.tag == f'{uri_mei}meterSig':
#								elem.set('label', f'<meterSig> from <choice> with @xml:id {choice_id}')
							if elem.tag == f'{uri_mei}scoreDef':
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
			if child.tag == f'{uri_mei}choice':
				choice_id = child.get(xml_id_key)
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

#							if choice_elem_child_copy.tag == f'{uri_mei}beam':
#								num_beamed_tabGrps = len(choice_elem_child_copy.findall('.//mei:tabGrp', ns))
			
							if choice_elem_child_copy.tag == f'{uri_mei}tabGrp':
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
#								if e.tag == f'{uri_mei}tabGrp':
#									b = ''
#									b = ', beamed, '
#									e.set('label', f'tabGrp {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
#									tabGrp_cnt += 1
#								if e.tag == f'{uri_mei}meterSig':
#									e.set('label', f'1/1 from <choice> with @xml:id {choice_id}')

							if choice_elem_child_copy.tag == f'{uri_mei}meterSig':
								choice_elem_child_copy.set('label', f'1/1 from <choice> with @xml:id {choice_id}')
							if choice_elem_child_copy.tag == f'{uri_mei}scoreDef':
								choice_elem_child_copy.set('label', f'<scoreDef> from <choice> with @xml:id {choice_id}')
							
							elem.insert(i + j, choice_elem_child_copy)
						# Remove the original <choice> element
						elem.remove(child)


def _find_first_elem_after(ind: int, elems_flat: list, tag: str):
	return next(
		(elem for elem in elems_flat[ind + 1:] if elem.tag == tag), 
		None
	)



