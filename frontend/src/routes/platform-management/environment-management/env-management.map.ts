export const environmentStatusMap = new Map<string, any>([
  [
    'creating',
    {
      text: "creating",
      type: 'creating',
    },
  ],
  [
    'CREATING',
    {
      text: "creating",
      type: 'creating',
    },
  ],
  [
    'updating',
    {
      text: "updating",
      type: 'creating',
    },
  ],
  [
    'UPDATING',
    {
      text: "updating",
      type: 'creating',
    },
  ],
  [
    'FAILED',
    {
      text: "creation_failed",
      type: 'error',
    },
  ],
  [
    'failed',
    {
      text: "error",
      type: 'creation_failed',
    },
  ],
  [
    'READY',
    {
      text: "ready",
      type: 'active',
    },
  ],
  [
    'ready',
    {
      text: "ready",
      type: 'active',
    },
  ],
  [
    'DELETING',
    {
      text: "deleting",
      type: 'cancel',
    },
  ],
  [
    'deleting',
    {
      text: "deleting",
      type: 'waiting',
    },
  ],
  [
    'DELETE_FAILED',
    {
      text: "delete_failed",
      type: 'error',
    },
  ],
]);
