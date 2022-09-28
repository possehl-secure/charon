/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.charon3.core.protocol.endpoints;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.charon3.core.config.SCIMCustomSchemaExtensionBuilder;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;
import org.wso2.charon3.core.config.SCIMGroupSchemaExtensionBuilder;
import org.wso2.charon3.core.encoder.JSONEncoder;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.exceptions.NotImplementedException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.protocol.ResponseCodeConstants;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.schema.SCIMConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.charon3.core.config.SCIMGroupSchemaExtensionBuilder;
import org.wso2.charon3.core.schema.AttributeSchema;

import static org.wso2.charon3.core.schema.SCIMConstants.CUSTOM_USER;
import static org.wso2.charon3.core.schema.SCIMConstants.CommonSchemaConstants.LOCATION;
import static org.wso2.charon3.core.schema.SCIMConstants.CommonSchemaConstants.META;
import static org.wso2.charon3.core.schema.SCIMConstants.CommonSchemaConstants.RESOURCE_TYPE;
import static org.wso2.charon3.core.schema.SCIMConstants.ENTERPRISE_USER;
import static org.wso2.charon3.core.schema.SCIMConstants.ENTERPRISE_USER_SCHEMA_URI;
import static org.wso2.charon3.core.schema.SCIMConstants.GROUP;
import static org.wso2.charon3.core.schema.SCIMConstants.GROUP_CORE_SCHEMA_URI;
import org.wso2.charon3.core.schema.SCIMConstants.ListedResourceSchemaConstants;
import static org.wso2.charon3.core.schema.SCIMConstants.OperationalConstants.ATTRIBUTES;
import static org.wso2.charon3.core.schema.SCIMConstants.USER;
import static org.wso2.charon3.core.schema.SCIMConstants.USER_CORE_SCHEMA_URI;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceSchemaManager;


/**
 * The schema resource enables a service
 * provider to discover SCIM specification features in a standardized
 * form as well as provide additional implementation details to clients.
 */
public class SchemaResourceManager extends AbstractResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(SchemaResourceManager.class);

    private String customUserSchemaURI = null;
    private String customGroupSchemaURI = null;
    
    public SchemaResourceManager() {

    }

    /**
     * Retrieves a SCIM schemas definition.
     *
     * @param id
     * @param userManager
     * @param attributes
     * @param excludeAttributes
     * @return SCIM schemas response.
     */
    @Override
    public SCIMResponse get(String id, UserManager userManager, String attributes, String excludeAttributes) {

        try {
            SCIMResourceTypeSchema userSchemaAttributes = SCIMResourceSchemaManager.getInstance().getUserResourceSchema();
            customUserSchemaURI = SCIMCustomSchemaExtensionBuilder.getInstance().getURI();

            SCIMResourceTypeSchema groupSchemaAttributes = SCIMResourceSchemaManager.getInstance().getGroupResourceSchema();
            customGroupSchemaURI = SCIMGroupSchemaExtensionBuilder.getInstance().getExtensionSchema().getURI();

            Map<String, SCIMResourceTypeSchema> schemas = new HashMap<>();
            // Below code blocks handles the /Schemas/ api requests.
            if (StringUtils.isBlank(id)) {
                schemas.put(USER_CORE_SCHEMA_URI, userSchemaAttributes);
                schemas.put(ENTERPRISE_USER_SCHEMA_URI, userSchemaAttributes);
                if (StringUtils.isNotBlank(customUserSchemaURI)) {
                    schemas.put(customUserSchemaURI, userSchemaAttributes);
                }
                schemas.put(GROUP_CORE_SCHEMA_URI, groupSchemaAttributes);
                if (StringUtils.isNotBlank(customGroupSchemaURI)) {
                   schemas.put(customGroupSchemaURI, groupSchemaAttributes);
                }
                return buildSchemasResponse(schemas);
            }

            // Below code blocks handles the /Schemas/{id} api requests.
            if (USER_CORE_SCHEMA_URI.equalsIgnoreCase(id)) {
                schemas.put(id, userSchemaAttributes);
            } else if (ENTERPRISE_USER_SCHEMA_URI.equalsIgnoreCase(id)) {
                schemas.put(ENTERPRISE_USER_SCHEMA_URI, userSchemaAttributes);
            } else if (StringUtils.isNotBlank(customUserSchemaURI) && customUserSchemaURI.equalsIgnoreCase(id)) {
                schemas.put(customUserSchemaURI, userSchemaAttributes);
            } else if (GROUP_CORE_SCHEMA_URI.equalsIgnoreCase(id)) {
                schemas.put(id, groupSchemaAttributes);
            } else if (StringUtils.isNotBlank(customGroupSchemaURI) && customGroupSchemaURI.equalsIgnoreCase(id)) {
                schemas.put(customGroupSchemaURI, groupSchemaAttributes);
            } else {
                // https://tools.ietf.org/html/rfc7643#section-8.7
                throw new NotImplementedException("only user, enterprise and custom schema are supported");
            }

            return buildSchemasResponse(schemas);
        } catch (CharonException | NotFoundException | NotImplementedException e) {
            // TODO: 11/7/19 Seperate out user errors & server errors
            return AbstractResourceManager.encodeSCIMException(e);
        }
    }

    /**
     * Builds the SCIM schemas response using the provided schemas.
     *
     * @param schemas Map of retrieved SCIM schemas
     * @return SCIM schemas response.
     * @throws CharonException
     * @throws NotFoundException
     */
    private SCIMResponse buildSchemasResponse(Map<String, SCIMResourceTypeSchema> schemas) 
            throws CharonException, NotFoundException {

        String schemaResponseBody = buildSchemasResponseBody(schemas).toString();
        Map<String, String> responseHeaders = getResponseHeaders();
        return new SCIMResponse(ResponseCodeConstants.CODE_OK, schemaResponseBody, responseHeaders);
    }
    
    /**
     * Builds the SCIM schemas config json representation using the provided schemas.
     *
     * @param schemas Map of retrieved schemas
     * @return SCIM schemas config json representation.
     * @throws CharonException
     */
    private JSONObject buildSchemasResponseBody(Map<String, SCIMResourceTypeSchema> schemas) throws CharonException, NotFoundException {
        
        JSONObject rootObject = new JSONObject();
        
        if (schemas.size() > 1) {
          rootObject.put("schemas", new JSONArray().put(SCIMConstants.LISTED_RESOURCE_CORE_SCHEMA_URI)); 
        }
        
        JSONArray schemaObject = new JSONArray();
        
        if (schemas.get(USER_CORE_SCHEMA_URI) != null) {
          schemaObject.put(buildUserSchema(schemas.get(USER_CORE_SCHEMA_URI)));
        }
        if (schemas.get(ENTERPRISE_USER_SCHEMA_URI) != null) {
          schemaObject.put(buildEnterpriseUserSchema(ENTERPRISE_USER_SCHEMA_URI, schemas.get(ENTERPRISE_USER_SCHEMA_URI)));
        }
        String customUserSchemaURI = SCIMCustomSchemaExtensionBuilder.getInstance().getURI();
        if (StringUtils.isNotBlank(customUserSchemaURI) && schemas.get(customUserSchemaURI) != null) {
          schemaObject.put(buildCustomUserSchema(schemas.get(customUserSchemaURI)));
        }
        if (schemas.get(GROUP_CORE_SCHEMA_URI) != null) {
          schemaObject.put(buildGroupSchema(schemas.get(GROUP_CORE_SCHEMA_URI)));
        }
        String customGroupSchemaURI = SCIMGroupSchemaExtensionBuilder.getInstance().getExtensionSchema().getURI();
        if (StringUtils.isNotBlank(customGroupSchemaURI) && schemas.get(customGroupSchemaURI) != null) {
          schemaObject.put(buildCustomGroupSchema(schemas.get(customGroupSchemaURI)));
        }
        
        if (schemas.size() > 1) {
          rootObject.put(ListedResourceSchemaConstants.TOTAL_RESULTS, schemas.size());
          rootObject.put(ListedResourceSchemaConstants.ITEMS_PER_PAGE, schemas.size());
          rootObject.put(ListedResourceSchemaConstants.START_INDEX, 1);
          rootObject.put(ListedResourceSchemaConstants.RESOURCES, schemaObject);
        } else {
          rootObject = schemaObject.getJSONObject(0);
        }
        
        return rootObject;
    }

    /**
     * Builds a JSON object containing enterprise user schema attribute information.
     *
     * @param enterpriseUserSchemaList Attribute list of SCIM enterprise user schema
     * @return JSON object of enterprise user schema
     * @throws CharonException
     */
    private JSONObject buildEnterpriseUserSchema(String customSchemaURI, SCIMResourceTypeSchema schema) throws CharonException, NotFoundException {

        try {
            JSONEncoder encoder = getEncoder();

            JSONObject schemaObject = new JSONObject();
            schemaObject.put(SCIMConstants.CommonSchemaConstants.ID, ENTERPRISE_USER_SCHEMA_URI);
            schemaObject.put(SCIMConstants.EnterpriseUserSchemaConstants.NAME, ENTERPRISE_USER);
            schemaObject.put(SCIMConstants.
                    EnterpriseUserSchemaConstants.DESCRIPTION, "Enterprise User Schema");

            JSONArray schemasArray = new JSONArray();
            schemasArray.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
            schemaObject.put(SCIMConstants.CommonSchemaConstants.SCHEMAS, schemasArray);
            
            JSONObject metaSchemaObject = new JSONObject();
            metaSchemaObject.put(RESOURCE_TYPE, "Schema");
            metaSchemaObject.put(LOCATION, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT) + "/" + ENTERPRISE_USER_SCHEMA_URI);
            schemaObject.put(META, metaSchemaObject);

            ArrayList<AttributeSchema> attributeSchemaList = schema.getAttributesList();
            ArrayList<AttributeSchema> attributeSubSchemaList = null;
            
            for (AttributeSchema attributeSchema : attributeSchemaList) {
              if (attributeSchema.getURI().contains(customSchemaURI)) {
                attributeSubSchemaList = (ArrayList<AttributeSchema>) attributeSchema.getSubAttributeSchemas();
              }
            }
            
            JSONArray schemaAttributeArray = buildSchemaAttributesArray(attributeSubSchemaList);
            schemaObject.put(ATTRIBUTES, schemaAttributeArray);
            
            return schemaObject;
        } catch (JSONException e) {
            throw new CharonException("Error while encoding enterprise user schema.", e);
        }
    }

    private JSONObject buildCustomUserSchema(SCIMResourceTypeSchema schema)
            throws CharonException, NotFoundException {

        try {
            JSONEncoder encoder = getEncoder();
            JSONObject schemaObject = new JSONObject();
            schemaObject.put(SCIMConstants.CommonSchemaConstants.ID, customUserSchemaURI);
            schemaObject.put(SCIMConstants.CustomUserSchemaConstants.NAME, CUSTOM_USER);
            schemaObject.put(SCIMConstants.CustomUserSchemaConstants.DESCRIPTION, "Custom User Schema");

            JSONArray schemasArray = new JSONArray();
            schemasArray.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
            schemaObject.put(SCIMConstants.CommonSchemaConstants.SCHEMAS, schemasArray);
            
            JSONObject metaSchemaObject = new JSONObject();
            metaSchemaObject.put(RESOURCE_TYPE, "Schema");
            metaSchemaObject.put(LOCATION, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT) + "/" + customUserSchemaURI);
            schemaObject.put(META, metaSchemaObject);

            ArrayList<AttributeSchema> attributeSchemaList = schema.getAttributesList();
            ArrayList<AttributeSchema> attributeSubSchemaList = null;
            
            for (AttributeSchema attributeSchema : attributeSchemaList) {
              if (attributeSchema.getURI().contains(customUserSchemaURI)) {
                attributeSubSchemaList = (ArrayList<AttributeSchema>) attributeSchema.getSubAttributeSchemas();
              }
            }
            
            JSONArray schemaAttributeArray = buildSchemaAttributesArray(attributeSubSchemaList);
            schemaObject.put(ATTRIBUTES, schemaAttributeArray);
            
            return schemaObject;
        } catch (JSONException e) {
            throw new CharonException("Error while encoding custom user schema.", e);
        }
    }

    private JSONObject buildUserSchema(SCIMResourceTypeSchema schema) throws CharonException, NotFoundException {

        try {
            JSONEncoder encoder = getEncoder();

            JSONObject schemaObject = new JSONObject();
            schemaObject.put(SCIMConstants.CommonSchemaConstants.ID, USER_CORE_SCHEMA_URI);
            schemaObject.put(SCIMConstants.UserSchemaConstants.NAME, USER);
            schemaObject.put(SCIMConstants.ResourceTypeSchemaConstants.DESCRIPTION, "User Schema");
            
            JSONArray schemasArray = new JSONArray();
            schemasArray.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
            schemaObject.put(SCIMConstants.CommonSchemaConstants.SCHEMAS, schemasArray);

            JSONObject metaSchemaObject = new JSONObject();
            metaSchemaObject.put(RESOURCE_TYPE, "Schema");
            metaSchemaObject.put(LOCATION, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT) + "/" + USER_CORE_SCHEMA_URI);
            schemaObject.put(META, metaSchemaObject);

            ArrayList<AttributeSchema> attributeSchemaList = schema.getAttributesList();
            ArrayList<AttributeSchema> attributeSubSchemaList = new ArrayList<AttributeSchema>();
            
            for (AttributeSchema attributeSchema : attributeSchemaList) {
              if (!attributeSchema.getURI().contains(ENTERPRISE_USER_SCHEMA_URI) && 
                  !(StringUtils.isNotBlank(customUserSchemaURI) && attributeSchema.getURI().contains(customUserSchemaURI))) {
                attributeSubSchemaList.add(attributeSchema);
              }
            }
            
            JSONArray schemaAttributeArray = buildSchemaAttributesArray(attributeSubSchemaList);
            schemaObject.put(ATTRIBUTES, schemaAttributeArray);
            
            return schemaObject;
        } catch (JSONException e) {
            throw new CharonException("Error while encoding user schema", e);
        }
    }

    private JSONObject buildGroupSchema(SCIMResourceTypeSchema schema) throws CharonException, NotFoundException {

        try {
            JSONEncoder encoder = getEncoder();

            JSONObject schemaObject = new JSONObject();
            schemaObject.put(SCIMConstants.CommonSchemaConstants.ID, GROUP_CORE_SCHEMA_URI);
            schemaObject.put(SCIMConstants.UserSchemaConstants.NAME, GROUP);
            schemaObject.put(SCIMConstants.ResourceTypeSchemaConstants.DESCRIPTION, "Group Schema");

            JSONArray schemasArray = new JSONArray();
            schemasArray.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
            schemaObject.put(SCIMConstants.CommonSchemaConstants.SCHEMAS, schemasArray);
            
            JSONObject metaSchemaObject = new JSONObject();
            metaSchemaObject.put(RESOURCE_TYPE, "Schema");
            metaSchemaObject.put(LOCATION, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT) + "/" + GROUP_CORE_SCHEMA_URI);
            schemaObject.put(META, metaSchemaObject);

            ArrayList<AttributeSchema> attributeSchemaList = schema.getAttributesList();    
            ArrayList<AttributeSchema> attributeSubSchemaList = new ArrayList<AttributeSchema>();
            
            for (AttributeSchema attributeSchema : attributeSchemaList) {
              if (!(StringUtils.isNotBlank(customGroupSchemaURI) && attributeSchema.getURI().contains(customGroupSchemaURI))) {
                attributeSubSchemaList.add(attributeSchema);
              }
            }
            
            JSONArray schemaAttributeArray = buildSchemaAttributesArray(attributeSubSchemaList);
            schemaObject.put(ATTRIBUTES, schemaAttributeArray);
            
            return schemaObject;
            
        } catch (JSONException e) {
            throw new CharonException("Error while encoding group schema", e);
        }
    }

    private JSONObject buildCustomGroupSchema(SCIMResourceTypeSchema schema)
            throws CharonException, NotFoundException {

        try {
            JSONEncoder encoder = getEncoder();

            JSONObject schemaObject = new JSONObject();
            schemaObject.put(SCIMConstants.CommonSchemaConstants.ID, customGroupSchemaURI);
            schemaObject.put(SCIMConstants.CustomUserSchemaConstants.NAME, "VodafoneGroup");
            schemaObject.put(SCIMConstants.ResourceTypeSchemaConstants.DESCRIPTION, "Vodafone Group Extension Schema");

            JSONArray schemasArray = new JSONArray();
            schemasArray.put("urn:ietf:params:scim:schemas:core:2.0:Schema");
            schemaObject.put(SCIMConstants.CommonSchemaConstants.SCHEMAS, schemasArray);
            
            JSONObject metaSchemaObject = new JSONObject();
            metaSchemaObject.put(RESOURCE_TYPE, "Schema");
            metaSchemaObject.put(LOCATION, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT) + "/" + customGroupSchemaURI);
            schemaObject.put(META, metaSchemaObject);

            ArrayList<AttributeSchema> attributeSchemaList = schema.getAttributesList();
            ArrayList<AttributeSchema> attributeSubSchemaList = null;
            
            for (AttributeSchema attributeSchema : attributeSchemaList) {
              if (attributeSchema.getURI().contains(customGroupSchemaURI)) {
                attributeSubSchemaList = (ArrayList<AttributeSchema>) attributeSchema.getSubAttributeSchemas();
              }
            }
            
            JSONArray schemaAttributeArray = buildSchemaAttributesArray(attributeSubSchemaList);
            schemaObject.put(ATTRIBUTES, schemaAttributeArray);
            
            return schemaObject;
        } catch (JSONException e) {
            throw new CharonException("Error while encoding custom group schema.", e);
        }
    }

    private Map<String, String> getResponseHeaders() throws NotFoundException {

        Map<String, String> responseHeaders;
        responseHeaders = new HashMap<>();
        responseHeaders.put(SCIMConstants.CONTENT_TYPE_HEADER, SCIMConstants.APPLICATION_JSON);
        responseHeaders.put(SCIMConstants.LOCATION_HEADER, getResourceEndpointURL(SCIMConstants.SCHEMAS_ENDPOINT));
        return responseHeaders;
    }

        /*
     *  Build the user schema json representation.
     * @return
     */
    public JSONArray buildSchemaAttributesArray(ArrayList<AttributeSchema> schemaAttributeList) throws JSONException {
      
        JSONObject schemaObject = new JSONObject();
        
        JSONArray schemaAttributes = new JSONArray();
                
        for (AttributeSchema schemaAttribute : schemaAttributeList) {
            
            JSONObject schemaJSONAttribute = new JSONObject();
            if (schemaAttribute.getType() == SCIMDefinitions.DataType.COMPLEX) {
              schemaJSONAttribute = encodeComplexAttributeSchema(schemaAttribute);
            } else {
              schemaJSONAttribute = encodeBasicAttributeSchema(schemaAttribute);
            }
            schemaAttributes.put(schemaJSONAttribute);
        }
        
        return schemaAttributes;
    }
    
        /**
     * Encode the attribute schema and return the json object.
     *
     * @param attribute
     * @return json object of the attribute schema.
     * @throws JSONException
     */
    public JSONObject encodeBasicAttributeSchema(AttributeSchema attribute) throws JSONException {

        JSONObject attributeSchema = new JSONObject();
        attributeSchema.put(org.wso2.charon3.core.schema.SCIMConstants.UserSchemaConstants.NAME, attribute.getName());
        attributeSchema.put(org.wso2.charon3.core.schema.SCIMConstants.CommonSchemaConstants.TYPE, attribute.getType());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.MULTIVALUED, attribute.getMultiValued());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.DESCRIPTION, attribute.getDescription());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.REQUIRED, attribute.getRequired());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.CASE_EXACT, attribute.getCaseExact());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.MUTABILITY, attribute.getMutability());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.RETURNED, attribute.getReturned());
        attributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.UNIQUENESS, attribute.getUniqueness());

        return attributeSchema;
    }

     /**
-     * Encode the multiValued attribute schema and return the json object.
+     * Encode the complex attribute schema and return the json object.
      *
      * @param complexAttribute
      * @return json object of the complex attribute schema.
      * @throws JSONException
      */
    public JSONObject encodeComplexAttributeSchema(AttributeSchema complexAttribute) throws JSONException {
 

        JSONObject complexAttributeSchema = encodeBasicAttributeSchema(complexAttribute);
        List<AttributeSchema> subAttributesSchemaList = complexAttribute.getSubAttributeSchemas();
        
        if (subAttributesSchemaList != null) {
          JSONArray subAttributesSchemaArray = new JSONArray();

          for (AttributeSchema subAttributeSchema : subAttributesSchemaList) {
            JSONObject subAttributesSchemaObject = encodeBasicAttributeSchema(subAttributeSchema);
            subAttributesSchemaArray.put(subAttributesSchemaObject);
          }
 
          complexAttributeSchema.put(org.wso2.charon3.core.config.SCIMConfigConstants.SUB_ATTRIBUTES, subAttributesSchemaArray);
         }
       
       return complexAttributeSchema;
     }


    @Override
    public SCIMResponse create(String scimObjectString, UserManager userManager, String attributes, String
            excludeAttributes) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse delete(String id, UserManager userManager) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse listWithGET(UserManager userManager, String filter, int startIndex, int count, String sortBy,
                                    String sortOrder, String domainName, String attributes, String excludeAttributes) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    /**
     * @param userManager       User manager
     * @param filter            Filter to be executed
     * @param startIndexInt     Starting index value of the filter
     * @param countInt          Number of required results
     * @param sortBy            SortBy
     * @param sortOrder         Sorting order
     * @param domainName        Domain name
     * @param attributes        Attributes in the request
     * @param excludeAttributes Exclude attributes
     * @return SCIM response
     */
    @Override
    public SCIMResponse listWithGET(UserManager userManager, String filter, Integer startIndexInt, Integer countInt,
                                    String sortBy, String sortOrder, String domainName, String attributes,
                                    String excludeAttributes) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse listWithPOST(String resourceString, UserManager userManager) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse updateWithPUT(String existingId, String scimObjectString, UserManager userManager, String
            attributes, String excludeAttributes) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse updateWithPATCH(String existingId, String scimObjectString, UserManager userManager, String
            attributes, String excludeAttributes) {

        String error = "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return encodeSCIMException(badRequestException);
    }
}
