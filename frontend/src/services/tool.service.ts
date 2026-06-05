import { ElementRef, Injectable } from '@angular/core';
import {
  IAssistant,
  IBuiltInTool,
  IModel,
  ITool,
  IViewTool,
} from '@interfaces/tool/tool.interface';
import { CommonValidation } from '@shared/validation/commonValidation';
import { I18NextEagerPipe } from 'angular-i18next';
import { I18nNamespace } from '@i18n';
import { MessageComponent } from '@shared/services/cfdata.service';
import { cdnAssetUrl } from 'src/single-spa/assets-url';
import i18next from 'i18next';

@Injectable({
  providedIn: 'root',
})
export class ToolService {
  public readonly EditorOptions = {
    theme: 'vs',
    language: 'json',
    overviewRulerLanes: 0,
    hideCursorInOverviewRuler: true,
    minimap: { enabled: false },
    tabSize: 2,
  };

  constructor(private readonly i18n: I18NextEagerPipe) {}

  public readonly FlowObjectJSONSchema = {
    uri: 'agent://flow/object-schema.json',
    fileMatch: ['object*.json'],
    schema: {
      type: 'object',
      additionalProperties: true,
      properties: {},
      not: {
        anyOf: [
          { type: 'array' },
          { type: 'string' },
          { type: 'number' },
          { type: 'boolean' },
          { type: 'null' },
        ],
      },
    },
  };

  public readonly FlowStringArrayJSONSchema = {
    uri: 'agent://flow/stringArr-schema.json',
    fileMatch: ['stringArr*.json'],
    schema: {
      type: 'array',
      items: {
        type: 'string',
      },
    },
  };

  public readonly FlowIntegerArrayJSONSchema = {
    uri: 'agent://flow/integerArr-schema.json',
    fileMatch: ['integerArr*.json'],
    schema: {
      type: 'array',
      items: {
        type: 'integer',
      },
    },
  };

  public readonly FlowFloatArrayJSONSchema = {
    uri: 'agent://flow/floatArr-schema.json',
    fileMatch: ['floatArr*.json'],
    schema: {
      type: 'array',
      items: {
        type: 'number',
      },
    },
  };

  public readonly FlowBooleanArrayJSONSchema = {
    uri: 'agent://flow/booleanArr-schema.json',
    fileMatch: ['booleanArr*.json'],
    schema: {
      type: 'array',
      items: {
        type: 'boolean',
      },
    },
  };

  public readonly FlowObjectArrayJSONSchema = {
    uri: 'agent://flow/objectArr-schema.json',
    fileMatch: ['objectArr*.json'],
    schema: {
      type: 'array',
      items: {
        type: 'object',
        additionalProperties: true,
        properties: {},
        not: {
          anyOf: [
            { type: 'array' },
            { type: 'string' },
            { type: 'number' },
            { type: 'boolean' },
            { type: 'null' },
          ],
        },
      },
    },
  };
}
