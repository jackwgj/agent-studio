import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ModelSquareService {

  /** 禁止调测的模型类型 */
  public static NOT_ALLOW_TEST_TYPE = ['TEXT-TO-IMAGE', 'TEXT-TO-VIDEO', 'Text-Multimodal-Embedding'];

  constructor() { }
}
