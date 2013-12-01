Ext.define('TreeModel', {
    extend: 'Ext.data.Model',
    fields: ['text', 'guid']
});
var url = "http://localhost:8084/MMDA424/rest/webservice/";

Ext.onReady(function() {
    var initConnReq = new XMLHttpRequest();
    initConnReq.open("POST", url + "initConnection", false);
    initConnReq.send();

    var jsonData = fillTree("37e21364-2be5-402e-ae84-020683315e07");
    var store = Ext.create('Ext.data.TreeStore', {
        model: 'TreeModel',
        root: jsonData
    });

    Ext.create('Ext.tree.Panel', {
        title: 'Simple Tree',
        width: 200,
        height: 150,
        store: store,
        rootVisible: false,
        renderTo: Ext.getBody(),
        listeners: {
            'select': function(a, record, b, c) {
                alert(record.get('text') + ' -> ' + record.get('guid'));
            }
        }
    });
    store.setRootNode(fillTree("103c2de2-3fa9-4e6a-9cb0-56d941cc4e51"));
});


fillTree = function(dagrId) {
    var toParse;
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function()
    {
        if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
        {
            toParse = xmlhttp.responseText;
        }
    };
    var params = '?dagrId=' + encodeURIComponent(dagrId);
    xmlhttp.open("GET", url + "dagrView" + params, false);
    xmlhttp.send();
    return eval('(' + toParse + ')');
};

