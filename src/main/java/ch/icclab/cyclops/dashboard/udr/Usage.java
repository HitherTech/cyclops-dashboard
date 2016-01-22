/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.dashboard.udr;

import ch.icclab.cyclops.dashboard.application.DashboardApplication;
import ch.icclab.cyclops.dashboard.cache.TLBInput;
import ch.icclab.cyclops.dashboard.errorreporting.ErrorReporter;
import ch.icclab.cyclops.dashboard.load.Loader;
import ch.icclab.cyclops.dashboard.oauth2.OAuthClientResource;
import ch.icclab.cyclops.dashboard.oauth2.OAuthServerResource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * This class handles all requests concerning the UDR microservice's usage data
 */
public class Usage extends OAuthServerResource {
    final static Logger logger = LogManager.getLogger(Usage.class.getName());
    private HashMap<String, TLBInput> hashMap = DashboardApplication.representationHashMap;

    /**
     * This method updates gets the usage data from the UDR microservice
     * <p>
     * The method receives the the user's keystone ID and two timestamps (from / to) from the dashboard frontend.
     * It then sends this information to the UDR Endpoint, requesting the usage data during the given time frame. The
     * UDR endpoint is configured in /WEB-INF/configuration.txt as UDR_USAGE_URL
     *
     * @param entity The incoming request from the dashboard frontend
     * @return A representation of the untouched response
     */
    @Post("json")
    public Representation getUsageData(Representation entity) {
        try {
            logger.trace("Attempting to Get the Usage data.");
            JsonRepresentation represent = new JsonRepresentation(entity);
            JSONObject requestJson = represent.getJsonObject();
            String keystoneId = requestJson.getString("keystoneId");
            String from = requestJson.getString("from");
            String to = requestJson.getString("to");
            to = dateToUTFString(stringToDate(to));
            Form form = new Form();
            form.add("from", from);
            form.add("to", to);
            if (!keystoneId.equals("undefined")) {
                String oauthToken = getOAuthTokenFromHeader();
                String url = Loader.getSettings().getCyclopsSettings().getUdr_usage_url() + keystoneId + "?" + form.getQueryString();
                OAuthClientResource clientResource = new OAuthClientResource(url, oauthToken);
                Representation representation;
                StringRepresentation result = null;
                if (Loader.getSettings().getCyclopsSettings().getCache().equalsIgnoreCase("true")) {
                    if (!hashMap.containsKey(keystoneId)) {
                        representation = clientResource.get();
                        logger.debug("Attempting to insert data in the <Userid,TLBInput> HashMap");
                        hashMap.put(keystoneId, new TLBInput(representation.getText(), stringToDate(from), stringToDate(to)));
                    } else {
                        Date minInserted = hashMap.get(keystoneId).getFrom();
                        Date maxInserted = hashMap.get(keystoneId).getTo();

                        if (stringToDate(from).getTime() < minInserted.getTime()) {
                            form.set("to", dateToString(minInserted));

                            url = Loader.getSettings().getCyclopsSettings().getUdr_usage_url() + keystoneId + "?" + form.getQueryString();
                            clientResource = new OAuthClientResource(url, oauthToken);
                            representation = clientResource.get();
                            hashMap.get(keystoneId).addData(representation.getText(), stringToDate(from), minInserted);
                        }
                        if (maxInserted.getTime() < stringToDate(to).getTime()) {
                            form.set("from", dateToString(maxInserted));

                            url = Loader.getSettings().getCyclopsSettings().getUdr_usage_url() + keystoneId + "?" + form.getQueryString();
                            clientResource = new OAuthClientResource(url, oauthToken);
                            representation = clientResource.get();
                            hashMap.get(keystoneId).addData(representation.getText(), maxInserted, stringToDate(to));
                        }
                    }
                    Date dateTo = stringToDate(to);
                    Date dateFrom = stringToDate(from);
                    result = new StringRepresentation(hashMap.get(keystoneId).getData(stringToDate(from), stringToDate(to)));
                    return result;
                } else {
                    String data = "";
                    Date dateFrom = stringToDate(from);
                    Date dateTo = stringToDate(to);
                    try {
                        representation = clientResource.get();
                        data = representation.getText();
//                        data = new TLBInput().formatRawData(representation.getText(), dateFrom, dateTo);
                    } catch (Exception ignored) {
                        //TODO this is here until better and more generic way how to work with different usecases is implemented
                        String response = pullData(url);
                        TLBInput input = new TLBInput();
                        data = input.formatRawData(response, dateFrom, dateTo);
                    }
                    if (data.equals(""))
                        return null;
                    else
                        return new StringRepresentation(data);

                }
            }
        } catch (Exception e) {
            logger.error("Error while getting usage data: " + e.getMessage());
            ErrorReporter.reportException(e);
            throw new ResourceException(500);
        }
        return new StringRepresentation("[]");
    }

    private String pullData(String url) throws IOException {
        logger.trace("Starting to pull data from provided URL");

        // create connection
        ClientResource cr = new ClientResource(url);
        Request req = cr.getRequest();

        // now header
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");

        // fire it up
        cr.get(MediaType.APPLICATION_JSON);
        Representation output = cr.getResponseEntity();

        logger.trace("Successfully pulled data from provided URL");

        // and return response data
        return output.getText();
    }

    /**
     * Private function that creates a Date from a String
     *
     * @param date
     * @return
     */
    private Date stringToDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Private function that formats a Date into a String
     *
     * @param date
     * @return
     */
    private String dateToString(Date date) {
        try {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String result = simpleDateFormat.format(date);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Private function that formats a Date into a String in UTF
     *
     * @param date
     * @return
     */
    private String dateToUTFString(Date date) {
        try {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String result = simpleDateFormat.format(date);
            return result;
        } catch (Exception e) {
            return null;
        }
    }


}
