export interface ISrcData {
  data: any[];
  state: {
    searched: boolean;
    sorted: boolean;
    paginated: boolean;
  };
}

export interface IObsTable {
  displayed: any[];
  srcData: ISrcData;
  searchWords: any[];
  searchKeys: any[];
}

export interface IObsDirTable {
  displayed: any[];
  srcData: ISrcData;
  searchWords: any[];
  searchKeys: any[];
}

export interface IFiles {
  content_length: null | number;
  file_name: string;
  file_type: string;
  last_modified: null | number;
}

export interface IFolders {
  content_length: null | number;
  file_name: string;
  file_type: string;
  last_modified: null | number;
}

export interface IFileListByBucketData {
  obs_object_map?: {
    files: IFiles[];
    folders: IFolders[];
  };
}
