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


def convert_tabGrp(tabGrp: ET.Element, not_type: str, is_beamed: bool): # -> str
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
			if not_type == NOTATIONTYPES[FLT]:
				tab_symbol = f'{"abcdefghikl"[int(fret)]}{course}.'
			elif not_type == NOTATIONTYPES[GLT]:
				tab_symbol = f'{NEWSIDLER[int(course)-1][int(fret)]}.'
			else:
				tab_symbol = f'{fret}{course}.'
			event_list[NUM_COURSES - int(course)] = tab_symbol
	
	# Make tbp_event		
	tab_symbols = ''
	for tab_symbol in event_list:
		if tab_symbol is not None:
			tab_symbols += tab_symbol

	return f'{rhythm_symbol}{tab_symbols}>.' 






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


def _find_first_elem_after(ind: int, elems_flat: list, tag: str):
	return next(
		(elem for elem in elems_flat[ind + 1:] if elem.tag == tag), 
		None
	)


scoreDefs = score.findall('.//mei:scoreDef', ns)
meterSigs = []
elems_flat = list(score.iter()) # flat list of all elements in document order
last_sd_ind = 0
for scoreDef in scoreDefs:
	curr_ms = scoreDef.find('.//mei:meterSig', ns) # None if none found
	n = None
	for i in range(last_sd_ind, len(elems_flat)):
		elem = elems_flat[i]	
		if elem.get(xml_id_key) == scoreDef.get(xml_id_key):
			last_sd_ind = i
			next_measure = _find_first_elem_after(i, elems_flat, f'{uri_mei}measure')
			n = next_measure.get('n')
			break
	meterSigs.append((n, curr_ms))


not_type = scoreDefs[0].find('.//mei:staffDef', ns).get('notationtype')

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


def find_parent(root, child):
	for parent in root.iter():
		for elem in parent:
			if elem is child:
				return parent

	return None


def implement_choice(root: ET.Element, choice_ids: list, tag: str):
    for elem in root.iter():
        for i, child in enumerate(list(elem)):
            if child.tag == f'{uri_mei}choice':
                choice_id = child.get(xml_id_key)
                if choice_id in choice_ids:
                    choice_elem = child.find(f'mei:{tag}', ns)
                    if choice_elem is not None:
                        choice_elem_copy = copy.deepcopy(choice_elem)

                        # Count all tabGrps first
                        num_tabGrps = len(choice_elem_copy.findall('.//mei:tabGrp', ns))
                        tabGrp_cnt = 1

                        # Now process the copied subtree with beam tracking
                        def label_elements(elem, inside_beam=False):
                            nonlocal tabGrp_cnt
                            if elem.tag == f'{uri_mei}beam':
                                inside_beam = True
                            if elem.tag == f'{uri_mei}tabGrp':
                                b = ', beamed, ' if inside_beam else ' '
                                elem.set('label', f'<tabGrp> {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
                                tabGrp_cnt += 1
                            elif elem.tag == f'{uri_mei}meterSig':
                                elem.set('label', f'<meterSig> from <choice> with @xml:id {choice_id}')
                            for child_elem in elem:
                                label_elements(child_elem, inside_beam)

                        label_elements(choice_elem_copy)

                        # Insert top-level children of <corr> or <sic> copy
                        for offset, e in enumerate(list(choice_elem_copy)):
                            elem.insert(i + offset, e)

                        # Remove the original <choice> element
                        elem.remove(child)


def implement_choice2(root: ET.Element, choice_ids: list, tag: str): # -> None
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
						num_beamed_tabGrps = 0
						for j, choice_elem_child in enumerate(islice(choice_elem.iter(), 1, None)): # iter() starts w/ choice_elem itself
#						for j, choice_elem_child in enumerate(choice_elem_contents):
							choice_elem_child_copy = copy.deepcopy(choice_elem_child)
							print(choice_elem_child_copy.tag)
							
							
							if choice_elem_child_copy.tag == f'{uri_mei}beam':
								num_beamed_tabGrps = len(choice_elem_child_copy.findall('.//mei:tabGrp', ns))
			
							if choice_elem_child_copy.tag == f'{uri_mei}tabGrp':
#								print("BLA")
								print(num_beamed_tabGrps)
								b = ''
								if num_beamed_tabGrps > 0:
#									print('jepperdepep')
									b = ', beamed, '
									num_beamed_tabGrps -= 1
								choice_elem_child_copy.set('label', f'tabGrp {tabGrp_cnt}/{num_tabGrps}{b}from <choice> with @xml:id {choice_id}')
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
							
							elem.insert(i + j, choice_elem_child_copy)
						# Remove the original <choice> element
						elem.remove(child)

TAG = 'corr'
implement_choice(root, choice_ids, TAG)


#for elem in root.iter():
#	print(elem)

for elem in root.iter():
	if elem.get('label') != None:
		print(elem.get(xml_id_key), elem.get('label'))
sss

# TS event
# RS event
# rest event
# MS event
# barline event

tbp = ''
sections = score.findall('mei:section', ns)




# Handle <sections>
# Possible direct children in <section>: <scoreDef>, <measure> (possibly within <choice>), <sb>
# TODO make arg_
def handle_section(section: ET.Element, choices: list, tbp: str): # -> str
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

#					beam_contents = list(beam)
#					# Possible elements in <beam>: <tabGrp> (possibly within <choice>), <sb>
#					for elem_beam in beam_contents:
#						# 2.2.1 <beam> element is <tabGrp>
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

# <section> can contain <choice> around one or more <measure>s
# <measure> can contain <choice> around any combination of <tabGrp> and <beam>
# <beam>    can contain <choice> around one or more (but not all) <tabGrp>s

# for elem in choice:
#   


# Possible direct children in <score>: <scoreDef>, <section>, <sb>
for elem_score in score:
	# 1. <score> element is <scoreDef>
	if elem_score.tag == f'{uri_mei}scoreDef':
		meterSig = elem_score.find('.//mei:meterSig', ns)
		ms = '' if meterSig == None else convert_meterSig(meterSig)
		tbp += ms
	# 2. <score> element is <section>
	if elem_score.tag == f'{uri_mei}section':
		s = handle_section(elem_score, choices, tbp)
		tbp += s
	# 3. <score> element is <sb>
	if elem_score.tag == f'{uri_mei}sb':
		print(elem_score.tag)
print('OE')
print(tbp)
sdfsdf

def convert_beam(beam: ET.Element): # -> str
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

# <choice> can contain: <metersig>, <measure>, <beam>, <tabGrp> 

# <score>   can have direct children: <scoreDef>, <section>, <sb>
# <section> can have direct children: <scoreDef>, <measure>, <sb>
# <measure> can have direct children: <beam>, <tabGrp>, <sb>
# <beam>    can have direct children: <tabGrp>, <sb>

#<score>
#	<scoreDef>
#	<section>
#		<scoreDef>
#		<measure> (<layer>)
#			<tabGrp>
#			<beam>
#				<tabGrp>
#				<sb>
#			<sb>
#		</measure>
#		<sb>	
#	</section>
#	<sb>

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











