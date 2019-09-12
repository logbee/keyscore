import {MetaData} from "@keyscore-manager-models/src/main/common";
import {Record} from "@keyscore-manager-models/src/main/dataset/";

export interface Dataset {
   metaData: MetaData;
   records: Record[];
}
