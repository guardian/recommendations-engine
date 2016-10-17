#!/bin/bash -e

STAGE=$1

aws emr create-cluster \
  --applications Name=Spark \
  --tags "App=spark" "Stage=$STAGE" "Stack=recommendations" \
  --ec2-attributes '{
    "KeyName": "MobileAppsApiKeyPair",
    "AdditionalSlaveSecurityGroups": [
      "sg-afac5dc9"
    ],
    "InstanceProfile": "recommendations-spark-PROD-JobFlowInstanceProfile-185YH7TWAH4ZN",
    "SubnetId": "subnet-edc7409a",
    "EmrManagedSlaveSecurityGroup": "sg-fa9d0e9d",
    "EmrManagedMasterSecurityGroup": "sg-fc9d0e9b",
    "AdditionalMasterSecurityGroups": [
      "sg-acac5dca"
    ]
  }' \
  --service-role EMR_DefaultRole \
  --release-label emr-5.0.0 \
  --log-uri 's3n://aws-mobile-logs/elasticmapreduce/' \
  --name "recommendations-spark-$STAGE" \
  --instance-groups '[
      {
        "InstanceCount": 4,
        "BidPrice": "0.15",
        "EbsConfiguration": {
          "EbsBlockDeviceConfigs": [
            {
              "VolumeSpecification": {
                "SizeInGB": 32,
                "VolumeType": "gp2"
              },
              "VolumesPerInstance": 1
            }
          ]
        },
        "InstanceGroupType": "CORE",
        "InstanceType": "m4.xlarge"
      },
      {
        "InstanceCount": 1,
        "BidPrice": "0.15",
        "EbsConfiguration": {
          "EbsBlockDeviceConfigs": [
            {
              "VolumeSpecification": {
                "SizeInGB": 32,
                "VolumeType": "gp2"
              },
              "VolumesPerInstance": 1
            }
          ]
        },
        "InstanceGroupType": "MASTER",
        "InstanceType": "m4.xlarge"
      }
    ]' \
  --region eu-west-1