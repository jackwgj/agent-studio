import i18next from 'i18next';

export const HTTP_CODE = {
  SUCCESS: 0,
};

export const DEFAULT_CODE = `def main(args: dict) -> dict:
    """
    ${i18next.t('default_code_annotation_1')}
    ${i18next.t('default_code_annotation_2')}
    ${i18next.t('default_code_annotation_3')}
    """
    ret = {
        "key0": args.get('input', 'default'),
        "key1": "hi"
    }
    return ret
`;

export const ERROR_MSG_DISPLAY_TIME = 3000;
