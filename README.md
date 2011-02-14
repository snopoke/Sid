Sid - a field based record matcher
==================================

Sid compares the field values of records and determines their similarity 
by calculating the [Sorensen similarity index](http://en.wikipedia.org/wiki/S%C3%B8rensen_similarity_index) 
between records. 

The Sorensen index is calculated as follows:

QS = 2C /(A + B) where
* C is the number of field values that recordA and recordB have in common
* A is the total number of fields for recordA
* B is the total number of fields for recrodB

Two records would have a similarity index of 1.0 if both records have the
same number of fields and all the fields have identical value.

Note that this algorithm does not consider field names or position. Should you wish to include 
field name / position in the comparison you can do so by 
appending / prepending the field name / position to the field value.

The algorithm used by Sid to find common fields is described in a paper by [Udi Manber](http://en.wikipedia.org/wiki/Udi_Manber)
titled [FINDING SIMILAR FILES IN A LARGE FILE SYSTEM](http://www.usenix.org/publications/library/proceedings/sf94/full_papers/manber.finding).

Example
-------
Assume records have 3 fields, F1, F2 and F3

Record A = {F1 => 'text', F2 => 'other', F3 => 453}
Record B = {F1 => 'other', F2 => 'text', F3 => 453}

To compare these records ignoring field name you could give the following input to Sid:
	
	fieldValue,recordId
	text,A
	other,A
	453,A
	other,B
	text,B
	453,B

This would result in an exact match. However should you wish to take into account the 
field positions the following input to Sid would give a similarity index = 2/6:
	
	fieldValue,recordId
	F1text,A
	F2other,A
	F3453,A
	F1other,B
	F2text,B
	F3453,B


 

   