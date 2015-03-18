/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import java.util.HashMap;

import org.junit.Before;

import university.dibs.dibs.Qualification;

public class QualificationTest extends CommonInformationTest {
    @Before
    public void before() {
        this.informationType = new Qualification.Type();
        this.createArgs = new HashMap<String, Object>();
        this.createArgs.put("grade", 4.0);
    }
}
