#!/bin/bash
./tools/kddcup_combine_ugroup test buffer.test -u uID cohort post -i iID thInfo imfb -max_block 1000000 -scale_score 1
./tools/svd_feature_infer config.conf pred=40

