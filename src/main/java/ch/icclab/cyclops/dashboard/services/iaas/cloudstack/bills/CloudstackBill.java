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

package ch.icclab.cyclops.dashboard.services.iaas.cloudstack.bills;


import ch.icclab.cyclops.dashboard.bills.BillInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CloudstackBill extends ch.icclab.cyclops.dashboard.bills.Bill{
    private HashMap<String, String> info;
    private HashMap<String, Double> usagePerMeter;
    private HashMap<String, Double> costPerMeter;
    private HashMap<String, String> unitPerMeter;
    private HashMap<String, Double> discounts;

    public CloudstackBill() {
        info = new HashMap<String, String>();
        usagePerMeter = new HashMap<String, Double>();
        costPerMeter = new HashMap<String, Double>();
        unitPerMeter = new HashMap<String, String>();
        discounts = new HashMap<String, Double>();
        discounts.put("overall", 0.0);

        /*
            Debug
         */
        info.put("org-name", "InIT Service Engineering");
        info.put("address-line1", "Obere Kirchgasse 2");
        info.put("address-line2", "CH Winterthur - 8401");
    }

    public void addItem(String meterName, Double usage, Double cost, String unit) {
        usagePerMeter.put(meterName, usage);
        costPerMeter.put(meterName, cost);
        unitPerMeter.put(meterName, unit);
    }

    public void addItem(String meterName, Double usage, Double rate, String unit, Double discount) {
        addItem(meterName, usage, rate, unit);
        discounts.put(meterName, discount);
    }

    public void addOverallDiscount(Double value) {
        discounts.put("overall", value);
    }

    public HashMap<String, String> getInfo() {
        return info;
    }

    public HashMap<String, Double> getUsage() {
        return usagePerMeter;
    }

    public HashMap<String, Double> getCosts() {
        return costPerMeter;
    }

    public HashMap<String, String> getUnits() {
        return unitPerMeter;
    }

    public HashMap<String, Double> getDiscounts() {
        return discounts;
    }

    public void setFromDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        cal.setTime(sdf.parse(date));
        info.put("bill-start-year", String.valueOf(cal.get(Calendar.YEAR)));
        info.put("bill-start-month", padToDoubleDigits(cal.get(Calendar.MONTH) + 1));
        info.put("period-start-date", padToDoubleDigits(cal.get(Calendar.DAY_OF_MONTH)));
    }

    public void setToDate(String date) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        cal.setTime(sdf.parse(date));
        info.put("bill-end-year", String.valueOf(cal.get(Calendar.YEAR)));
        info.put("bill-end-month", padToDoubleDigits(cal.get(Calendar.MONTH) + 1));
        info.put("period-end-date", padToDoubleDigits(cal.get(Calendar.DAY_OF_MONTH)));
    }

    public void setDueDate(String date) {
        info.put("payment-date", date);
    }

    public String getFromDate() {
        return info.get("bill-start-year") + "-" + info.get("bill-start-month") + "-" + info.get("period-start-date");
    }

    public String getToDate() {
        return info.get("bill-end-year") + "-" + info.get("bill-end-month") + "-" + info.get("period-end-date");
    }

    public String getDueDate() {
        return info.get("payment-date");
    }

    public void setApproved(boolean isApproved) {
        info.put("approved", isApproved ? "1" : "0");
    }

    public void setPaid(boolean isPaid) {
        info.put("paid", isPaid ? "1" : "0");
    }

    public boolean isApproved() {
        return !info.get("approved").equals("0");
    }

    public boolean isPaid() {
        return !info.get("paid").equals("0");
    }

    public void setRecipientName(String firstName, String lastName) {
        info.put("person-name", firstName + " " + lastName);
    }

    public String getRecipientName() {
        return info.get("person-name");
    }

    private String padToDoubleDigits(int i) {
        String numStr = String.valueOf(i);
        return i < 10 ? "0" + numStr : numStr;
    }
}