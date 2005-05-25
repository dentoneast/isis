package org.nakedobjects.distribution;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.persistence.defaults.MockField;
import org.nakedobjects.object.reflect.NakedObjectField;

import junit.framework.TestCase;


public class ObjectDataFactoryTest extends TestCase {

    private TestingObjectDataFactory factory;
    private DummyNakedObjectSpecification specification;
    private MockNakedObject object;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ObjectDataFactoryTest.class);
    }

    protected void setUp() throws Exception {
        factory = new TestingObjectDataFactory();

        specification = new DummyNakedObjectSpecification();
        specification.fields = new NakedObjectField[0];

        object = new MockNakedObject();
        object.setupSpecification(specification);
    }
    
    public void testBasicObject() {
        MockOid oid = new MockOid(1);
        object.setOid(oid);

        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(oid, od.getOid());
        assertEquals(specification.getFullName(), od.getType());
        assertEquals(false, od.isResolved());
        assertEquals(0, od.getVersion());
        assertEquals(0, od.getFieldContent().length);
    }

    public void testResolved() {
        object.setResolved();
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(true, od.isResolved());
    }
    
    public void testVersion() {
        object.setVersion(78821L);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(78821L, od.getVersion());
     
    }


    public void testObjectWithEmptyFields() {
        specification.fields = new NakedObjectField[] {
                new MockField(), new MockField(), new MockField()
        };
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(3, od.getFieldContent().length);
    }

    public void testObjectWithFields() {
        specification.fields = new NakedObjectField[] {
                new MockField(), new MockField(), new MockField()
        };
        
        MockNakedObject fieldObject = new MockNakedObject();
        DummyNakedObjectSpecification fieldSpecification = new DummyNakedObjectSpecification();
        fieldSpecification.fields = new NakedObjectField[0];
        fieldObject.setupSpecification(fieldSpecification);
        
        object.setupField("", fieldObject);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(3, od.getFieldContent().length);
        ObjectData objectData = ((ObjectData) od.getFieldContent()[1]);
        assertEquals(fieldSpecification.getFullName(), objectData.getType());
  
    }


}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */