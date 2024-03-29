/****************************************************************
 *
 * Copyright (c) 2011, 2012
 *
 * School of Engineering, Cardiff University, UK
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Project name: srs EU FP7 (www.srs-project.eu)
 * ROS stack name: NA
 * ROS package name: knowledge_server
 * Description: 
 *								
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * @author Ze Ji, email: jiz1@cf.ac.uk
 *
 * Date of creation: Oct 2011:
 * ToDo: 
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *	 * Redistributions of source code must retain the above copyright
 *	   notice, this list of conditions and the following disclaimer.
 *	 * Redistributions in binary form must reproduce the above copyright
 *	   notice, this list of conditions and the following disclaimer in the
 *	   documentation and/or other materials provided with the distribution.
 *	 * Neither the name of the school of Engineering, Cardiff University nor 
 *         the names of its contributors may be used to endorse or promote products 
 *         derived from this software without specific prior written permission.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License LGPL as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License LGPL for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License LGPL along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ****************************************************************/

package org.srs.knowledge_server.ontology;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.rdf.model.Property;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.ontology.OntResource;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.srs.knowledge_server.ros.*;

public class OntologyDB
{
    public OntologyDB()
    {
	// create an empty model
	this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
    }

    public OntologyDB(String filename)
    {
	try {
	    //String modelFileName = filename;
	    this.reloadOWLFile(filename);
	}
	catch(IllegalArgumentException e) {
	    System.out.println("Caught Exception : " + e.getMessage());
	}
    }

    public OntologyDB(ArrayList<String> filenames)
    {
	// create an empty model
	this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

	//this.model = ModelFactory.createDefaultModel();
	try {
	    for(String filename : filenames) {
		//String modelFileName = filename;
		this.importOntology(filename);
	    }
	}
	catch(IllegalArgumentException e) {
	    System.out.println("Caught Exception : " + e.getMessage());
	}
    }

    public void importOntology(String filename) 
    {
	System.out.println("Load OWL File: " + filename);
	// use the FileManager to find the input file
	InputStream in = FileManager.get().open(filename);
	if (in == null) {
	    throw new IllegalArgumentException("File: " + filename + " not found");
	}
	
	// read the RDF/XML file
	model.read(in, null);
    }

    public String executeQuery(String queryString)
    {
	//// new added for test JSON output
	try{
	    Query query = null; 
	    QueryExecution qe = null;
	    ResultSet results = null;
	    
	    try {
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, model);
		results = qe.execSelect();
	    }
	    catch(Exception e) {
		System.err.println("Query Syntax invalid. \nCaught Exception:  " + e.toString() + "  \n" + e.getMessage());
		if(qe != null) {
		    qe.close();
		}
		return  "";
	    }
	    if (results == null) {
		qe.close();
		return "";
	    }
	    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
	    
	    ResultSetFormatter.outputAsJSON(ostream, results);
	    String r = new String(ostream.toByteArray(), "UTF-8");
	    qe.close();
	    return r;
	}
	catch(Exception e) {
	    System.out.println(e.toString());
	    return "";
	}
    }

    public boolean executeConstructQuery(String queryString)
    {
	//// new added for test JSON output
	Query query = null; 
	QueryExecution qe = null;
	//ResultSet results = null;
	com.hp.hpl.jena.rdf.model.Model res = null;
	try {
	    query = QueryFactory.create(queryString);
	    qe = QueryExecutionFactory.create(query, model);
	    res = qe.execConstruct();
	    model.add(res);
	}
	catch(Exception e) {
	    System.err.println("Query Syntax invalid. \nCaught Exception:  " + e.toString() + "  \n" + e.getMessage());
	    if(qe != null) {
		qe.close();
	    }
	    return  false;
	}
	if (res == null) {
	    qe.close();
	    return false;
	}
	res.write(System.out, "TURTLE");	
	qe.close();
	return true;
    }
    
    public ArrayList<QuerySolution> executeQueryRaw(String queryString)
    {
	try {
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qe = QueryExecutionFactory.create(query, model);
	ResultSet results = qe.execSelect();
	ArrayList<QuerySolution> resList = (ArrayList)ResultSetFormatter.toList(results);
	qe.close();
	return resList; //results;
	}
	catch(Exception e) {
	    System.out.println(e.toString());
	    return  new ArrayList<QuerySolution>();
	}

    }

    public boolean executeSparQLRule(String sparQLRule) {
	return true;
    }

    public void reloadOWLFile(String file)
    {
	// create an empty model
	//this.model = ModelFactory.createDefaultModel();
	this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

	System.out.println("Load OWL File: " + file);
	// use the FileManager to find the input file
	InputStream in = FileManager.get().open(file);
	if (in == null) {
	    throw new IllegalArgumentException("File: " + file + " not found");
	}
	
	// read the RDF/XML file
	model.read(in, null);
    }
    
    public void printModel()
    { 
	model.write(System.out);
    }

    public Iterator getInstancesOfClass(String className) 
    {
	// get the instances of a class
	OntClass onto = model.getOntClass( className );
	
	if(onto == null) {
	    System.out.println("ONT CLASS IS NULL");
	    return (new ArrayList()).iterator();
	}
	
	Iterator instances = onto.listInstances();
	return instances;
    }

    public String getNamespaceByPrefix(String namespacePrefix)
    {
	model.enterCriticalSection(Lock.READ);
	//http://www.srs-project.eu/ontologies/ipa-kitchen-map.owl#
	String pre = model.getNsPrefixURI(namespacePrefix);
	model.leaveCriticalSection();
	return pre;
    }

    /**
     * @param proNameSpace property namespace
     * @param proLocalName property name
     * @param ind individual object
     * @return statement containing the property info 
     */
    public com.hp.hpl.jena.rdf.model.Statement getPropertyOf(String proNameSpace, String proLocalName, Individual ind ) 
    {
	model.enterCriticalSection(Lock.READ);
	com.hp.hpl.jena.rdf.model.Property property = model.getProperty(proNameSpace, proLocalName);
	com.hp.hpl.jena.rdf.model.Statement stm = ind.getProperty(property);
	model.leaveCriticalSection();
	return stm;
    }

    public OntModel getModel() {
	return model;
    }
   
    public void insertInstance(String classURI, String className, String instanceURI, String instanceName) throws DuplicatedEntryException, UnknownClassException
    {
	model.enterCriticalSection(Lock.WRITE);
	Resource rs = model.getResource(classURI + className);
	if(rs == null) {
	    model.leaveCriticalSection();
	    throw new UnknownClassException(className);
	}
	
	Individual ind = model.getIndividual(instanceURI + instanceName);
	if(ind != null) {
	    model.leaveCriticalSection();
	    throw new  DuplicatedEntryException(instanceName);
	}
	ind = model.createIndividual(instanceURI + instanceName, rs);	
	ind.setOntClass(rs);
	model.leaveCriticalSection();
    }

    public void deleteInstance(String instanceURI, String instanceName) throws NonExistenceEntryException, UnknownException
    {	
	model.enterCriticalSection(Lock.WRITE);
	Individual ind = model.getIndividual(instanceURI + instanceName);
	if(ind == null) {
	    model.leaveCriticalSection();
	    throw new  NonExistenceEntryException(instanceName);
	}
	ind.remove();
	
	model.leaveCriticalSection();
    }
    
    public boolean removeStatement(Statement stm) 
    {
	// TODO: error checking in future
	model.enterCriticalSection(Lock.WRITE);
	model.remove(stm);
	model.leaveCriticalSection();
	return true;
    }

    public Individual getIndividual(String uri) throws NonExistenceEntryException
    {
	model.enterCriticalSection(Lock.READ);
	Individual ind = model.getIndividual(uri);
	if(ind == null) {
	    model.leaveCriticalSection();
	    throw new NonExistenceEntryException(uri);
	}
	model.leaveCriticalSection();
	return ind;
    }

    public Property getProperty(String uri) throws NonExistenceEntryException 
    {
	model.enterCriticalSection(Lock.READ);
	Property pro = model.getProperty(uri);
	if (pro == null) {
	    model.leaveCriticalSection();
	    throw new NonExistenceEntryException(uri);
	}
	model.leaveCriticalSection();
	return pro;
    }

    public OntModel model;
}
