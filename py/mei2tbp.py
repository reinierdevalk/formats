import os
import xml.etree.ElementTree as ET
from io import StringIO
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


def convertTabGrp(tabGrp: ET.Element, not_type: str, is_beamed: bool): # -> str
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
		if elem.tag == uri + 'note':
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
uri = f'{{{ns['mei']}}}'
#uri = '{' + ns['mei'] + '}'

# Get the tree, root (<mei>), and main MEI elements (<meiHead>, <score>)
tree, root = parse_tree(mei_str)
meiHead = root.find('mei:meiHead', ns)
music = root.find('mei:music', ns)
score = music.findall('.//' + uri + 'score')[0] 

scoreDefs = score.findall('.//' + uri + 'scoreDef')
not_type = score.findall('.//' + uri + 'staffDef')[0].get('notationtype')
meterSigs = []
for scoreDef in scoreDefs:
	curr_ms = scoreDef.findall('.//' + uri + 'meterSig')
	meterSigs.append(None if len(curr_ms) == 0 else curr_ms[0])

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


tbp = ''
sections = score.findall('mei:section', ns)
for i, section in enumerate(sections):
	# MensurationSign
	curr_ms = meterSigs[i] 
	if curr_ms != None:
		ms_key = get_meterSig_key(curr_ms)
		tbp += f'{MENSURATION_SIGNS[ms_key]}.>.' 
	for measure in section.findall('mei:measure', ns):
		layer = measure.findall('.//' + uri + 'layer')[0]
		# Possible elements: <tabGrp>, <beam>, <choice> 
		for elem in layer:
			# <tabGrp>
			if elem.tag == uri + 'tabGrp':
#				print(convertTabGrp(elem, not_type, False))
				pass
			# <beam>
			if elem.tag == uri + 'beam':
				tabGrps = list(elem)
				for tabGrp in tabGrps:
#					print(convertTabGrp(tabGrp, not_type, (False if tabGrp == tabGrps[-1] else True)))
					pass
			# <choice>
			if elem.tag == uri + 'choice':
				print('choice')

