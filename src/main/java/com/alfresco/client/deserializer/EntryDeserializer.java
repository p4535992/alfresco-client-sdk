/*
 *   Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 *   This file is part of Alfresco Java Client.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.alfresco.client.deserializer;

import java.lang.reflect.Type;

import com.alfresco.client.constant.PublicAPIConstant;
import com.alfresco.client.utils.ISO8601Utils;
import com.google.gson.*;

/**
 * Created by jpascal on 19/01/2016.
 */
public class EntryDeserializer<T> implements JsonDeserializer<T>
{
    @Override
    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException
    {
        JsonElement entry = je;
        if (je.getAsJsonObject().has(PublicAPIConstant.ENTRY_VALUE))
        {
            entry = je.getAsJsonObject().get(PublicAPIConstant.ENTRY_VALUE);
        }
        return new GsonBuilder().setDateFormat(ISO8601Utils.DATE_ISO_FORMAT).create().fromJson(entry, type);
    }
}