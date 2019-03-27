import {Descriptor} from "../../../../modules/keyscore-manager-models/src/main/descriptors/Descriptor";
import {FilterDescriptorJsonClass, ResolvedFilterDescriptor} from "../../../../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {
    ExpressionType, FieldNameHint,
    ParameterDescriptorJsonClass
} from "../../../../modules/keyscore-manager-models/src/main/parameters/ParameterDescriptor";

export const removeFieldFilterDescriptorJson = `{
  "jsonClass":"io.logbee.keyscore.model.descriptor.Descriptor",
  "ref":{
    "uuid":"b7ee17ad-582f-494c-9f89-2c9da7b4e467"
  },
  "describes":{
    "jsonClass":"io.logbee.keyscore.model.descriptor.FilterDescriptor",
    "name":"io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsFilterLogic",
    "displayName":{
      "id":"displayName"
    },
    "description":{
      "id":"description"
    },
    "categories":[
      {
        "name":"contrib.remove-drop",
        "displayName":{
          "id":"contrib.category.remove-drop.displayName"
        }
      }
    ],
    "parameters":[
      {
        "jsonClass":"io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
        "ref":{
          "id":"removeFields.fieldsToRemove"
        },
        "info":{
          "displayName":{
            "id":"fieldsToRemoveName"
          },
          "description":{
            "id":"fieldsToRemoveDescription"
          }
        },
        "descriptor":{
          "jsonClass":"io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
          "ref":{
            "id":""
          },
          "defaultValue":"",
          "hint":"PresentField",
          "mandatory":false
        },
        "min":1,
        "max":2147483647
      }
    ]
  },
  "localization":{
    "locales":[
      {
        "language":"en",
        "country":""
      },
      {
        "language":"de",
        "country":""
      }
    ],
    "mapping":{
      "contrib.category.data-extraction.displayName":{
        "translations":{
          "en":"Data-Extraction",
          "de":"Datengewinnung"
        }
      },
      "fieldKeyName":{
        "translations":{
          "en":"tbd",
          "de":"tbd"
        }
      },
      "description":{
        "translations":{
          "en":"Removes all given fields and their values",
          "de":"Filter zum entfernen von Feldern einschließlich ihrer Werte."
        }
      },
      "contrib.category.sink.displayName":{
        "translations":{
          "en":"Sink",
          "de":"Senke"
        }
      },
      "fieldKeyDescription":{
        "translations":{
          "en":"tbd",
          "de":"tbd"
        }
      },
      "fieldsToRemoveDescription":{
        "translations":{
          "en":"Field will be removed by filter",
          "de":"Feld wird vom Filter entfernt"
        }
      },
      "contrib.category.filter.displayName":{
        "translations":{
          "en":"Filter",
          "de":"Filter"
        }
      },
      "contrib.category.json.displayName":{
        "translations":{
          "en":"JSON",
          "de":"JSON"
        }
      },
      "contrib.category.source.displayName":{
        "translations":{
          "en":"Source",
          "de":"Quelle"
        }
      },
      "contrib.category.fields.displayName":{
        "translations":{
          "en":"Fields",
          "de":"Felder"
        }
      },
      "contrib.category.batch-composition.displayName":{
        "translations":{
          "en":"Batch-Composition",
          "de":"Stapelbildung"
        }
      },
      "displayName":{
        "translations":{
          "en":"Remove Fields",
          "de":"Felder entfernen"
        }
      },
      "contrib.category.remove-drop.displayName":{
        "translations":{
          "en":"Remove/Drop",
          "de":"Entfernen/Verwerfen"
        }
      },
      "contrib.category.math.displayName":{
        "translations":{
          "en":"Math",
          "de":"Mathematik"
        }
      },
      "fieldsToRemoveName":{
        "translations":{
          "en":"fields to be removed",
          "de":"Feld das entfernt werden soll"
        }
      },
      "contrib.category.visualization.displayName":{
        "translations":{
          "en":"Visualization",
          "de":"Visualisierung"
        }
      },
      "contrib.category.debug.displayName":{
        "translations":{
          "en":"Debug",
          "de":"Debug"
        }
      }
    }
  }
}`;

export const resolvedRemoveFieldsFilterDE: ResolvedFilterDescriptor = {
    descriptorRef: {
        uuid: "b7ee17ad-582f-494c-9f89-2c9da7b4e467"
    },
    name: "io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsFilterLogic",
    jsonClass: FilterDescriptorJsonClass.FilterDescriptor,
    displayName: "Felder entfernen",
    description: "Filter zum entfernen von Feldern einschließlich ihrer Werte.",
    categories: [{
        name: "contrib.remove-drop",
        displayName: "Entfernen/Verwerfen"
    }],
    parameters:[
        {
            ref:{
                id:"removeFields.fieldsToRemove"
            },
            info:{
                displayName:"Feld das entfernt werden soll",
                description:"Feld wird vom Filter entfernt"
            },
            jsonClass:ParameterDescriptorJsonClass.FieldNameListParameterDescriptor,
            descriptor:{
                jsonClass:ParameterDescriptorJsonClass.FieldNameParameterDescriptor,
                ref:{
                    id:""
                },
                info:{
                  displayName:"",
                  description:""
                },
                defaultValue:"",
                validator:null,
                hint:FieldNameHint.PresentField,
                mandatory:false
            },
            min:1,
            max:2147483647
        }
    ]
};