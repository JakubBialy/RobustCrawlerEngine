package com.jbialy.rce;

import java.util.List;
import java.util.Map;

public interface GenericHttpRequest<U> {

    //    URI getUri();
    U getUri();

//    String getName();

//    D getRequestData();

    Map<String, List<String>> getHeaders();

//    RequestMethod getRequestMethod();

//    enum RequestMethod {
//        GET, POST
//    }
}
