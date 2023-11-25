module dev.mccue.microhttp.flash {
    requires dev.mccue.microhttp.session;
    requires transitive dev.mccue.microhttp.handler;
    requires transitive dev.mccue.json;

    exports dev.mccue.microhttp.flash;
}