#!/bin/bash
./tools/kddcup_combine_ugroup train buffer.train -u uID cohort post -i iID thInfo imfb -max_block 1000000 -scale_score 1
mkdir model
./tools/svd_feature config.conf num_round=50

