package ch.icclab.cyclops.dashboard.application;

import ch.icclab.cyclops.dashboard.bills.Billing;
import ch.icclab.cyclops.dashboard.charge.Charge;
import ch.icclab.cyclops.dashboard.externalMeters.ExternalMeterSources;
import ch.icclab.cyclops.dashboard.externalMeters.ExternalUserAccounts;
import ch.icclab.cyclops.dashboard.login.Session;
import ch.icclab.cyclops.dashboard.rate.Rate;
import ch.icclab.cyclops.dashboard.rate.RateStatus;
import ch.icclab.cyclops.dashboard.udr.UdrMeter;
import ch.icclab.cyclops.dashboard.udr.Usage;
import ch.icclab.cyclops.dashboard.usecases.tnova.bills.BillInformation;
import ch.icclab.cyclops.dashboard.usecases.tnova.bills.BillPDF;
import ch.icclab.cyclops.dashboard.users.Admin;
import ch.icclab.cyclops.dashboard.users.User;
import ch.icclab.cyclops.dashboard.users.UserInfo;

/**
 * @author Manu
 * Created by root on 16.11.15.
 */
public class TnovaDashboardApplication extends AbstractApplication {
    @Override
    public void createRoutes() {
        router.attach("/usage", Usage.class);
        router.attach("/rate", Rate.class);
        router.attach("/rate/status", RateStatus.class);
        router.attach("/charge", Charge.class);
//        router.attach("/keystonemeters", KeystoneMeter.class);
        router.attach("/udrmeters", UdrMeter.class);
        router.attach("/udrmeters/externalids", ExternalUserAccounts.class);
        router.attach("/udrmeters/externalsources", ExternalMeterSources.class);
//        router.attach("/keystone", KeystoneAssociation.class);
        router.attach("/session", Session.class);
        router.attach("/users", User.class);
        router.attach("/users/{user}", UserInfo.class);
        router.attach("/admins", Admin.class);
        router.attach("/billing", Billing.class);
        router.attach("/billing/bills", BillInformation.class);
        router.attach("/billing/bills/pdf", BillPDF.class);
    }
}
