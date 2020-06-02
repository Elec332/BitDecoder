/**
 * Created by Elec332 on 1-6-2020
 */
module nl.elec332.lib.bitdecoder {

    exports nl.elec332.lib.bitdecoder.api;
    exports nl.elec332.lib.bitdecoder.impl; //todo: Maybe use services? But then other won't be able to extend these...

    requires nl.elec332.lib.eleclib;

}