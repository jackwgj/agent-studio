export enum ModelType {
  LLM = 'LLM',
  Text_Embedding = 'Text-Embedding',
  RERANK = 'RERANK',
  TEXT_TO_IMAGE = 'TEXT-TO-IMAGE',
  IMAGE_TO_TEXT = 'IMAGE-TO-TEXT',
  AUDIO_TO_TEXT = 'AUDIO-TO-TEXT',
  TEXT_TO_AUDIO = 'TEXT-TO-AUDIO',
  TEXT_IMAGE_EMBEDDING = 'TEXT-IMAGE-EMBEDDING',
}


export const enum ModelServiceType {
  privateIntegation = 'PRIVATE-INTEGRATION', //租户接入
  platformIntegation = 'PLATFORM-INTEGRATION', //平台接入
  platformIntegationKoogallery = 'PLATFORM-INTEGRATION-KOOGALLERY', //平台接入云商店
  privateDeploy = 'PRIVATE-DEPLOY', //租户部署
  platformDeploy = 'PLATFORM-DEPLOY', //平台部署
  autoDeploy = 'AUTO-INTERGATION',
}
