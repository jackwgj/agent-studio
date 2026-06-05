import { cdnAssetUrl } from "../single-spa/assets-url";
import i18next from 'i18next';
const running = cdnAssetUrl('assets/images/status/running.svg')
const stopped = cdnAssetUrl('assets/images/status/stopped.svg')
const warning = cdnAssetUrl('assets/images/status/warning.svg')
const intermediate = cdnAssetUrl('assets/images/status/intermediate.svg')
const failed = cdnAssetUrl('assets/images/status/failed.svg')
const start = cdnAssetUrl('assets/images/status/start.svg')
const stop = cdnAssetUrl('assets/images/status/stop.svg')
const publish = cdnAssetUrl('assets/images/status/publishNew.svg')
const cancel = cdnAssetUrl('assets/images/model/cancel.svg')


const statusMap = {
  running: {
    statusText: i18next.t('running'),
    statusIcon: running,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  deploying: {
    statusText: i18next.t('constants_status_1'),
    statusIcon: intermediate,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  failed: {
    statusText: i18next.t('constants_status_2'),
    statusIcon: failed,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  stopped: {
    statusText: i18next.t('constants_status_3'),
    statusIcon: stopped,
    operateText: i18next.t('enable'),
    operateIcon: start
  },
  stopping: {
    statusText: i18next.t('constants_status_4'),
    statusIcon: intermediate,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  deleting: {
    statusText: i18next.t('deleting'),
    statusIcon: intermediate,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  concerning: {
    statusText: i18next.t('constants_status_5'),
    statusIcon: warning,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  pending: {
    statusText: i18next.t('constants_status_6'),
    statusIcon: intermediate,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  waiting: {
    statusText: i18next.t('constants_status_7'),
    statusIcon: intermediate,
    operateText: i18next.t('disable'),
    operateIcon: stop
  },
  finished: {
    statusText: i18next.t('constants_status_8'),
    statusIcon: stopped,
    operateText: i18next.t('enable'),
    operateIcon: start
  },
  offline: {
    statusText: i18next.t('not_published'),
    statusIcon: stopped,
    operateText: i18next.t('publish'),
    operateIcon: publish
  },
  online: {
    statusText: i18next.t('published'),
    statusIcon: running,
    operateText: i18next.t('cancel_publish'),
    operateIcon: cancel
  }
}

const statusNewMap = {
  offline: {
    statusText: i18next.t('not_published'),
    statusIcon: stopped,
    operateText: i18next.t('publish'),
    operateIcon: publish
  },
  online: {
    statusText: i18next.t('published'),
    statusIcon: running,
    operateText: i18next.t('cancel_publish'),
    operateIcon: cancel
  }
}


const statusMeta = {
  running: 'running',
  deploying: 'deploying',
  failed: 'failed',
  stopped: 'stopped',
  stopping: 'stopping',
  deleting: 'deleting',
  concerning: 'concerning',
  pending: 'pending',
  waiting: 'waiting',
  finished: 'finished',
  offline: 'offline',
  online: 'online'
}
const typeMeta = {
  LLM: i18next.t('constants_status_9'),
  GENERAL: i18next.t('constants_status_10'),
  MULTIMODAL: i18next.t('constants_status_11'),
}
const modelSourceName = {
  'fine-tuning': i18next.t('constants_status_12'),
  obs: i18next.t('constants_status_13'),
  my_created:i18next.t('constants_status_14'),
}
export { statusMap, statusNewMap , statusMeta, typeMeta, modelSourceName }
