export interface ITmplCard {
    [key: string]: any;
    desc: string;
    name: string;
    agentId: string;
    description: string;
    creatorId: string;
    status: string;
    agentLogo: string;
    url: string;
  }

  export interface SkillSearchParams {
    offset: number;
    limit: number;
    name?: string;
    sort_key?: string;
    sort_dir?: string;
    published_asset: number
  }

  export interface SkillInfo{
    name: string;
    description: string
  }

  export interface QueryParams{
    offset: number;
    limit: number;
    [key: string]: any
}

export interface SkillCard {
  [key: string]: any;
  description: string;
}

export interface UnzippedData {
  [path: string]: Uint8Array | string;
}
  
