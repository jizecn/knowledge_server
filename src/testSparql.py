#!/usr/bin/env python
import roslib;
roslib.load_manifest('knowledge_server')
import sys
import rospy
from knowledge_server.srv import *

def querySparQL():
    rospy.wait_for_service('query_sparql')
    try:
        print '\n---- Try SparQL to query all instances of type Table ----'
        spql = rospy.ServiceProxy('query_sparql', QuerySparQL)
        #queryString = "PREFIX house: <http://www.semanticweb.org/ontologies/house.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n SELECT ?table \n WHERE { ?table rdf:type house:Table . }"
        queryString = """
        PREFIX srs: <http://www.srs-project.eu/ontologies/srs.owl#>  
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
        PREFIX ipa-kitchen-map: <http://www.srs-project.eu/ontologies/ipa-kitchen-map.owl#> 
        SELECT ?objs ?x ?y ?z ?w ?h ?l ?id 
        WHERE { 
                    ?objs rdf:type srs:Milkbox . 
                    ?objs srs:xCoord ?x . 
                    ?objs srs:yCoord ?y . 
                    ?objs srs:zCoord ?z . 
                    ?objs srs:widthOfObject ?w .
                    ?objs srs:heightOfObject ?h .
                    ?objs srs:lengthOfObject ?l .
                    }
                    """
        print queryString
        print '----\n'
        resp1 = spql(queryString)
        return resp1.result
    except rospy.ServiceException, e:
        print "Service call failed: %s"%e

if __name__ == "__main__":
    print querySparQL()
