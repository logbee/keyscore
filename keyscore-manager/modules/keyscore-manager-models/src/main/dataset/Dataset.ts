import {MetaData} from "@keyscore-manager-models/src/main/common/MetaData";
import {Record} from "@keyscore-manager-models/src/main/dataset/Record";

export interface Dataset {
   metaData: MetaData;
   records: Record[];
}
