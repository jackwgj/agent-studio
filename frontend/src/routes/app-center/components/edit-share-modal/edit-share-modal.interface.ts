export interface ShareListReq {
  offset: number;
  limit: number;
  resource_type: string;
  resource_name?: string;
  tag_id?: string;
}

export interface ShareListRes {

}
