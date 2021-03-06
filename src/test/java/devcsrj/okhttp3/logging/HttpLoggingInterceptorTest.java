/**
 * Copyright © 2017 Reijhanniel Jearl Campos (devcsrj@apache.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package devcsrj.okhttp3.logging;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.Test;

import com.alfresco.client.api.logging.HttpLoggingInterceptorSLF4J;

import java.io.IOException;
import java.io.InputStream;

//import static org.junit.Assert.assertNotEquals;

public class HttpLoggingInterceptorTest {

    @Test
    public void testIntercept() throws IOException {
        MockWebServer server = new MockWebServer();
        Buffer buffer = new Buffer();
        try (InputStream is = getClass().getResourceAsStream("/petstore.json")) {
            buffer.readFrom(is);
        }

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(buffer));

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptorSLF4J())
                .build();
        Request request = new Request.Builder()
                .get()
                .url(server.url("/petstore.json"))
                .build();

        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            InputStream inputStream = body.byteStream();
            //assertNotEquals(inputStream.available(), 0); // body must still be readable
        }
    }
}