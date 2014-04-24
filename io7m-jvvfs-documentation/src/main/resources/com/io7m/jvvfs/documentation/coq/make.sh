#!/bin/sh -ex
ocamlopt -o make str.cmxa make.ml
./make < model-actual.v_in
