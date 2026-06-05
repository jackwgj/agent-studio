export const ziMu = ['A', 'B', 'C', 'D', 'E']
export const getNewServiceKeyList = (list)=>{
    const _service_key_list =[]
    if(Array.isArray(list)){
        list.forEach((item,index)=>{
            _service_key_list.push(`${ziMu[index]}: ${item.service_name}`)
        })
    }
    return _service_key_list
}
