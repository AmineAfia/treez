package org.treez.core.atom.attribute;

import org.apache.log4j.Logger;
import org.junit.Before;

/**
 * Tests the Abstract Atom by creating a simple test implementation TestAtom.
 */
public class SectionTest extends AbstractAttributeContainerAtomConstructonTest {

	/**
	 * Logger for this class
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SectionTest.class);

	//#region SETUP

	@Override
	@Before
	public void createTestAtom() {

		//create test atom
		Section section = new Section(atomName);
		section.setLabel("Section Title");
		section.setDescription("Section Description");
		section.setLayout("HORIZONTAL");
		section.setExpanded(true);
		atom = section;
	}

	//#end region

	//#region TESTS

	//#end region

}
