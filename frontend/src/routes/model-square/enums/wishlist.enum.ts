export enum WishlistAbility {
  REASON = 'Reason', // 思考模式
  FUNCTION_CALL = 'FunctionCall', // Function Call
  JSON_FORMAT = 'JsonFormat', // 结构化输出
  PREFIX_CACHE = 'PrefixCache', // 前缀缓存
  PREFIX_RW = 'PrefixRW', // 前缀读写
}

export enum WishlistModelLength {
  FOUR_K = '4K',
  EIGHT_K = '8K',
  SIXTEEN_K = '16K',
  THIRTY_TWO_K = '32K',
  SIXTY_FOUR_K = '64K',
  HUNDRED_AND_TWENTY_EIGHT_K = '128K',
  TWO_HUNDRED_AND_FIFTY_SIX_K = '256K',
}
