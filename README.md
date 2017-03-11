# NEM Modules
====================
[![Build Status](https://travis-ci.org/NemProject/nem.modules.svg?branch=master)](https://travis-ci.org/NemProject/nem.modules)

A collection of modules developed by the NEM community for interacting with the NEM blockchain.

compilation:

    mvn install


## Multisig Creator Module

Creates a new multisig account given the desired cosigners and a funding account to pay the conversion fee.

usage:

    $publicKey1 = <hex-string>
    $publicKey2 = <hex-string>
    $publicKey3 = <hex-string>
    $fundingPrivateKey = <hex-string>

    nem-modules-mcm-0.6.84-BETA.jar \
        -n 3 -m 2 -cosigners $publicKey1 $publicKey2 $publicKey3 \
        -signer $fundingPrivateKey \
        -host 37.187.70.29 -port 7890
