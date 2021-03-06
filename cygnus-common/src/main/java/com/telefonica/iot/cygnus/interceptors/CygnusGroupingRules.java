/**
 * Copyright 2016 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FI-WARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.interceptors;

import com.telefonica.iot.cygnus.log.CygnusLogger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author frb
 */
public class CygnusGroupingRules {
    
    private static final CygnusLogger LOGGER = new CygnusLogger(CygnusGroupingRules.class);
    private LinkedList<CygnusGroupingRule> groupingRules;
    private long lastIndex;
    
    /**
     * Constructor.
     * @param groupingRulesFileName
     */
    public CygnusGroupingRules(String groupingRulesFileName) {
        groupingRules = new LinkedList<CygnusGroupingRule>();
        lastIndex = 0;
        
        // read the grouping rules file
        // a JSONParse(groupingRulesFileName) method cannot be used since the file may contain comment lines
        // starting by the '#' character (comments)
        String jsonStr = readGroupingRulesFile(groupingRulesFileName);

        if (jsonStr == null) {
            LOGGER.info("No grouping rules have been read");
            return;
        } // if

        LOGGER.info("Grouping rules read: " + jsonStr);

        // parse the Json containing the grouping rules
        JSONArray jsonGroupingRules = (JSONArray) parseGroupingRules(jsonStr);

        if (jsonGroupingRules == null) {
            LOGGER.warn("Grouping rules syntax has errors");
            return;
        } // if

        LOGGER.info("Grouping rules syntax is OK");

        // create a list of grouping rules, with precompiled regex
        setRules(jsonGroupingRules);
        LOGGER.info("Grouping rules regex'es have been compiled");
    } // CygnusGroupingRules
    
    /**
     * Gets the rule matching the given string.
     * @param servicePath
     * @param entityId
     * @param entityType
     * @return The grouping rule matching the give string
     */
    public CygnusGroupingRule getMatchingRule(String servicePath, String entityId, String entityType) {
        if (groupingRules == null) {
            return null;
        } // if
        
        for (CygnusGroupingRule rule : groupingRules) {
            String s = concatenateFields(rule.getFields(), servicePath, entityId, entityType);
            Matcher matcher = rule.getPattern().matcher(s);

            if (matcher.matches()) {
                return rule;
            } // if
        } // for

        return null;
    } // getMatchingRule
    
    private String concatenateFields(ArrayList<String> fields, String servicePath, String entityId,
            String entityType) {
        String concat = "";

        for (String field : fields) {
            if (field.equals("entityId")) {
                concat += entityId;
            } else if (field.equals("entityType")) {
                concat += entityType;
            } else if (field.equals("servicePath")) {
                concat += servicePath;
            } // if else
        } // for
        
        return concat;
    } // concatenateFields
    
    /**
     * Adds a new rule to the grouping rules.
     * @param rule
     */
    public void addRule(CygnusGroupingRule rule) {
        lastIndex++;
        rule.setId(this.lastIndex);
        this.groupingRules.add(rule);
    } // CygnusGroupingRule
    
    /**
     * Deletes a rule given its ID.
     * @param id
     * @return True, if the rule was deleted, otherwise false
     */
    public boolean deleteRule(long id) {
        if (groupingRules == null) {
            return false;
        } // if
        
        for (int i = 0; i < groupingRules.size(); i++) {
            CygnusGroupingRule groupingRule = groupingRules.get(i);
            
            if (groupingRule.getId() == id) {
                groupingRules.remove(i);
                return true;
            } // if
        } // for
        
        return false;
    } // deleteRule
    
    /**
     * Updates a rule given its ID.
     * @param id
     * @param rule
     * @return True, if the rule was updated, otherwise false
     */
    public boolean updateRule(long id, CygnusGroupingRule rule) {
        if (groupingRules == null) {
            return false;
        } // if
        
        for (int i = 0; i < groupingRules.size(); i++) {
            CygnusGroupingRule groupingRule = groupingRules.get(i);
            
            if (groupingRule.getId() == id) {
                groupingRules.remove(i);
                rule.setId(id);
                groupingRules.add(i, rule);
                return true;
            } // if
        } // for
        
        return false;
    } // updateRule
    
    /**
     * Gets a stringified version of the grouping rules.
     * @param asField
     * @return A stringified version of the grouping rules
     */
    public String toString(boolean asField) {
        if (groupingRules == null) {
            if (asField) {
                return "\"grouping_rules\": []";
            } else {
                return "{\"grouping_rules\": []}";
            } // if else
        } else {
            if (asField) {
                return "\"grouping_rules\": " + groupingRules.toString();
            } else {
                return "{\"grouping_rules\": " + groupingRules.toString() + "}";
            } // if else
        } // if else
    } // toString
    
    /**
     * Gets the grouping rules as a list of CygnusGroupingRule objects. It is protected since it is only used
     * by the tests.
     * @return The grouping rules as a list of CygnusGroupingRule objects
     */
    protected LinkedList<CygnusGroupingRule> getRules() {
        return groupingRules;
    } // getRules

    private void setRules(JSONArray jsonRules) {
        groupingRules = new LinkedList<CygnusGroupingRule>();

        for (Object jsonGroupingRule : jsonRules) {
            JSONObject jsonRule = (JSONObject) jsonGroupingRule;
            int err = CygnusGroupingRule.isValid(jsonRule, false);

            if (err == 0) {
                CygnusGroupingRule rule = new CygnusGroupingRule(jsonRule);
                groupingRules.add(rule);
                lastIndex = rule.getId();
            } else {
                switch (err) {
                    case 1:
                        LOGGER.warn("Invalid grouping rule, some field is missing. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 2:
                        LOGGER.warn("Invalid grouping rule, some field is empty. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 3:
                        LOGGER.warn("Invalid grouping rule, some field is not allowed. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                        break;
                    case 4:
                        LOGGER.warn("Invalid grouping rule, the fiware-servicePath does not start with '/'. "
                                + "It will be discarded. Details:" + jsonRule.toJSONString());
                        break;
                    default:
                        LOGGER.warn("Invalid grouping rule. It will be discarded. Details:"
                                + jsonRule.toJSONString());
                } // switch
            } // if else
        } // for
    } // setRules
    
    private String readGroupingRulesFile(String groupingRulesFileName) {
        if (groupingRulesFileName == null) {
            return null;
        } // if

        String jsonStr = "";
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(groupingRulesFileName));
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found. Details=" + e.getMessage() + ")");
            return null;
        } // try catch

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                } // if

                jsonStr += line;
            } // while

            return jsonStr;
        } catch (IOException e) {
            LOGGER.error("Error while reading the Json-based grouping rules file. Details=" + e.getMessage() + ")");
            return null;
        } // try catch
    } // readGroupingRulesFile

    private JSONArray parseGroupingRules(String jsonStr) {
        if (jsonStr == null) {
            return null;
        } // if

        JSONParser jsonParser = new JSONParser();

        try {
            return (JSONArray) ((JSONObject) jsonParser.parse(jsonStr)).get("grouping_rules");
        } catch (ParseException e) {
            LOGGER.error("Error while parsing the Json-based grouping rules file. Details=" + e.getMessage());
            return null;
        } // try catch
    } // parseGroupingRules

} // CygnusGroupingRules
