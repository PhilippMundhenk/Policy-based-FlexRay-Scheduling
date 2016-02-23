# Policy-based FlexRay Scheduling
This is a scheduling approach for FlexRay. A virtual communication layer allows priority-based scheduling in the FlexRay static segment. See [Reference](#reference) for details.

## Requirements
- The integrated ILP approach requires [SAT4J](http://www.sat4j.org/).
- The FlexrayVerifier requires [Ptolemyplot](http://ptolemy.eecs.berkeley.edu/java/ptplot/) in version 5.7.1 for plotting
- The FlexrayVerifier furthermore requires the [Real-Time Calculus Toolbox](http://www.mpa.ethz.ch/Rtctoolbox/Overview)

## Issues
- Could use some tidying up

## Reference
Please refer to this project either via this repository or via the paper it was built for:

Philipp Mundhenk, Florian Sagstetter, Sebastian Steinhorst, Martin Lukasiewycz, Samarjit Chakraborty. "Policy-based Message Scheduling Using FlexRay". In: Proceedings of the 12th International Conference on Hardware/Software Codesign and System Synthesis (CODES+ISSS 2014). India, pp. 19:1–19:10. DOI: 10.1145/2656075.2656094

### BibTeX: <br />
@inproceedings{msslc:2014, <br />
	doi = { 10.1145/2656075.2656094 }, <br />
	pages = { 19:1--19:10 }, <br />
	year = { 2014 }, <br />
	location = { India }, <br />
	booktitle = { Proceedings of the 12th International Conference on Hardware/Software Codesign and System Synthesis (CODES+ISSS 2014) }, <br />
	author = { Philipp Mundhenk and Florian Sagstetter and Sebastian Steinhorst and Martin Lukasiewycz and Samarjit Chakraborty }, <br />
	title = { Policy-based Message Scheduling Using FlexRay }, <br />
}