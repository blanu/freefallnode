exports.map = function (params)
{
    console.log('params: '+params);
    if(params['type']=='object')
    {
      console.log('object');
      var chunkX=params['chunkX'];
      var chunkY=params['chunkY'];
      var chunkId=chunkX+'_'+chunkY;

      return ["{\"key\": \""+chunkId+"\", \"value\": "+JSON.stringify(params)+"}"];
    }
    else
    {
      console.log('not object');
      return ["null"];
    }
}
