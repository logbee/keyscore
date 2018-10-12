import {Record} from "./Record";
import {MetaData} from "../common/MetaData";

export interface Dataset {
   metaData: MetaData;
   records: Record[];
}
