/*
 * (c) Copyright 2022. TWINSEC GmbH, All Rights Reserved.
 *
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.charon3.core.schema;

/**
 * this defines the pre-defined values specified in https://tools.ietf.org/html/rfc7643.
 */
public class SCIMDefinitions {

    /**
     * data types that an attribute can take, according to the SCIM spec.
     */
  
    public static enum DataType {
      
        STRING("string"),
        DECIMAL("decimal"),
        BOOLEAN("boolean"),
        INTEGER("integer"),
        DATE_TIME("dateTime"),
        BINARY("binary"),
        REFERENCE("reference"),
        COMPLEX("complex");
               
        private String value;
              
        private DataType(final String value) {
            this.value = value;
        }
               
        @Override
        public String toString() {
            return this.value;
        }
        
        public static DataType getEnum(final String val) {
      
            for (final DataType v : values()) {
                if (v.value.equalsIgnoreCase(val)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
        
    }
            
    /**
     * values that an attributes' mutability attribute can take.
     */
    public static enum Mutability {
        
        READ_ONLY("readOnly"),
        READ_WRITE("readWrite"),
        IMMUTABLE("immutable"),
        WRITE_ONLY("writeOnly");
               
        private String value;
              
        private Mutability(final String value) {
            this.value = value;
        }
               
        @Override
        public String toString() {
            return this.value;
        }
        
        public static Mutability getEnum(final String val) {
      
            for (final Mutability v : values()) {
                if (v.value.equalsIgnoreCase(val)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
        
    }

    /**
     * values that an attributes' returned attribute can take.
     */
    public static enum Returned {
        
        ALWAYS("always"),
        NEVER("never"),
        DEFAULT("default"),
        REQUEST("request");
               
        private String value;
              
        private Returned(final String value) {
            this.value = value;
        }
               
        @Override
        public String toString() {
            return this.value;
        }
        
        public static Returned getEnum(final String val) {
      
            for (final Returned v : values()) {
                if (v.value.equalsIgnoreCase(val)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
        
    }

    /**
     * values that an attributes' uniqueness attribute can take.
     */
    public static enum Uniqueness {
        
        NONE("none"),
        SERVER("server"),
        GLOBAL("global");
               
        private String value;
              
        private Uniqueness(final String value) {
            this.value = value;
        }
               
        @Override
        public String toString() {
            return this.value;
        }
        
        public static Uniqueness getEnum(final String val) {
      
            for (final Uniqueness v : values()) {
                if (v.value.equalsIgnoreCase(val)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
        
    }

    /**
     * SCIM resource types that a referenceType attribute that may be referenced.
     */
    public static enum ReferenceType {
        
        USER("User"),
        GROUP("Group"),
        ROLE("Role"),
        EXTERNAL("external"),
        URI("uri");
               
        private String value;
              
        private ReferenceType(final String value) {
            this.value = value;
        }
               
        @Override
        public String toString() {
            return this.value;
        }
        
        public static ReferenceType getEnum(final String val) {
      
            for (final ReferenceType v : values()) {
                if (v.value.equalsIgnoreCase(val)) {
                    return v;
                }
            }
            
            throw new IllegalArgumentException();
        }
        
    }

}
