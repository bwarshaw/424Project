var tabInterface;
var url = "http://localhost:8084/MMDA424/rest/webservice/";
var host = "http://129.2.239.234:8084/MMDA424/localWrites/";
var selections = new Array();

Ext.define('TreeModel', {
    extend: 'Ext.data.Model',
    fields: ['text', 'guid', 'type', 'date', 'size', 'annotations']
});

Ext.define('SingleDagrModel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'guid', type: 'string'},
        {name: 'path', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'size', type: 'string'},
        {name: 'date', type: 'string'},
        {name: 'annotations', type: 'string'}
    ]
});


Ext.onReady(function() {
    var initConnReq = new XMLHttpRequest();
    initConnReq.open("POST", url + "initConnection", false);
    initConnReq.send();
    Ext.tip.QuickTipManager.init();  // enable tooltips

    tabInterface = Ext.create('Ext.tab.Panel', {
        layoutOnTabChange: true,
        renderTo: document.body,
        items: [{
                title: 'User Interface',
                id: 'genUI',
                items: [createDagrButton, createDagrField,
                    searchOrphansButton, searchSterileButton,
                    searchDagrsButton, fromDateField, toDateField,
                    searchNameLabel, searchNameField, searchTypeLabel,
                    searchTypeField, searchCommentLabel, searchCommentField,
                    minSizeLabel, minSizeField, maxSizeLabel, maxSizeField,
                    bulkLoadButton, bulkLoadField, removeDuplicatesButton,
                    openTabButton, openTabField, htmlEditor, htmlEditorWriteButton]
            }
        ]
    });
});

var htmlEditor = Ext.create('Ext.form.HtmlEditor', {
    width: 580,
    height: 250,
    frame: true
});

var htmlEditorWriteButton = Ext.create('Ext.Button', {
    text: 'Save HTML',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                    alert(xmlhttp.responseText);
                }
                else {
                    initPanel(xmlhttp.responseText);
                }
            }
        };
        xmlhttp.open("POST", url + "saveHtml", true);
        var param = htmlEditor.getValue();
        xmlhttp.send(param);
    }
});

var openTabButton = Ext.create('Ext.Button', {
    text: 'Open DAGR in tab',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                    alert(xmlhttp.responseText);
                } else {
                    initPanel(xmlhttp.responseText);
                }
            }
        };
        xmlhttp.open("POST", url + "initTab", true);
        var param = openTabField.getRawValue();
        xmlhttp.send(param);
    }
});

var openTabField = Ext.create('Ext.form.field.Text', {
    id: "openTabField"
});
var removeDuplicatesButton = Ext.create('Ext.Button', {
    text: 'Remove Duplicates',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
//                for (var treeId in selections) {
//                    var tree = Ext.getCmp(treeId);
//                    var dagrId = treeId.substring(8);
//                    tree.getStore().setRootNode(getTreeView(dagrId));
//                }
                Ext.create('Ext.window.Window', {
                    title: 'Records Condensed',
                    height: 450,
                    width: 600,
                    layout: 'fit',
                    overflowY: 'auto',
                    html: xmlhttp.responseText
                }).show();
            }
        };
        xmlhttp.open("GET", url + "removeDuplicates", true);
        xmlhttp.send();
    }
});

var searchOrphansButton = Ext.create('Ext.Button', {
    text: 'Search for orphaned DAGRs',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                var resultWindow = createGridWindow('Orphan DAGRs', xmlhttp.responseText);
                resultWindow.show();
            }
        };
        xmlhttp.open("GET", url + "searchOrphans", true);
        xmlhttp.send();
    }
});

var searchSterileButton = Ext.create('Ext.Button', {
    text: 'Search for sterile DAGRs',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                var resultWindow = createGridWindow('Sterile DAGRs', xmlhttp.responseText);
                resultWindow.show();
            }
        };
        xmlhttp.open("GET", url + "searchSterile", true);
        xmlhttp.send();
    }
});

var createDagrField = Ext.create('Ext.form.field.Text', {
    id: "createDagrField"
});

var createDagrButton = Ext.create('Ext.Button', {
    text: 'Create DAGR',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                    alert(xmlhttp.responseText);
                }
                else {
                    initPanel(xmlhttp.responseText);
                }
            }
        };
        xmlhttp.open("POST", url + "createDagr", true);
        var param = createDagrField.getRawValue();
        xmlhttp.send(param);
    }
});


var fromDateField = Ext.create('Ext.form.field.Date', {
    fieldLabel: 'From',
    name: 'from_date'
});

var toDateField = Ext.create('Ext.form.field.Date', {
    fieldLabel: 'To',
    name: 'to_date'
});

var searchNameField = Ext.create('Ext.form.field.Text', {
    id: 'searchNameField'
});

var searchNameLabel = Ext.create('Ext.form.Label', {
    id: 'searchNameLabel',
    text: 'Name'
});

var searchTypeField = Ext.create('Ext.form.field.Text', {
    id: 'searchTypeField'
});

var searchTypeLabel = Ext.create('Ext.form.Label', {
    id: 'searchTypeLabel',
    text: 'Type'
});

var searchCommentField = Ext.create('Ext.form.field.Text', {
    id: 'searchCommentField'
});

var minSizeField = Ext.create('Ext.form.field.Text', {
    id: 'minSizeField'
});

var minSizeLabel = Ext.create('Ext.form.Label', {
    id: 'minSizeLabel',
    text: 'Min Size'
});

var maxSizeField = Ext.create('Ext.form.field.Text', {
    id: 'maxSizeField'
});

var maxSizeLabel = Ext.create('Ext.form.Label', {
    id: 'maxSizeLabel',
    text: 'Max Size'
});


var searchCommentLabel = Ext.create('Ext.form.Label', {
    id: 'searchCommentLabel',
    text: 'Keywords (split by comma)'
});


var searchDagrsButton = Ext.create('Ext.Button', {
    text: 'Search DAGRs',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                var resultWindow = createGridWindow('Search Results', xmlhttp.responseText);
                resultWindow.show();
            }
        };
        var params = '?fromDate=' + encodeURIComponent(fromDateField.getRawValue());
        params += '&toDate=' + encodeURIComponent(toDateField.getRawValue());
        params += '&name=' + encodeURIComponent(searchNameField.getRawValue());
        params += '&type=' + encodeURIComponent(searchTypeField.getRawValue());
        params += '&minSize=' + encodeURIComponent(minSizeField.getRawValue());
        params += '&maxSize=' + encodeURIComponent(maxSizeField.getRawValue());
        params += '&keywords=' + encodeURIComponent(searchCommentField.getRawValue().split(','));
        xmlhttp.open("GET", url + "searchDagr" + params, true);
        xmlhttp.send();
    }
});

/*
 * =======================================================================
 * DAGR SPECIFIC THINGS
 * =======================================================================
 */
function initPanel(dagrData) {
    var rootNode = eval('(' + dagrData + ')');
    var name = rootNode['children'][0]['text'];
    var guid = rootNode['children'][0]['guid'];
    var tree = createTreeView(guid, rootNode);
    var panel = Ext.create('Ext.panel.Panel', {
        title: name,
        id: guid,
        closable: true,
        items: [createAddDagrButton(), createAddDagrField(guid),
            createAnnotateDagrButton(), createAnnotateDagrField(guid),
            createReachedDagrsButton(), createReachingDagrsButton(),
            createRenameDagrButton(), createRenameDagrField(guid), createDeleteDagrButton(),
            tree, createDocViewerPanel(guid)]
    });
    tabInterface.add(panel);
    tabInterface.setActiveTab(panel);
}
;

createDocViewerPanel = function(dagrId) {
    var docViewerPanel = Ext.create('Ext.panel.Panel', {
        id: 'docViewer' + dagrId,
        title: 'Document Viewer',
        items: []
    });
    return docViewerPanel;
};


createAddDagrField = function(dagrId) {
    var addDagrField = Ext.create('Ext.form.field.Text', {
        id: "addDagrField" + dagrId
    });
    return addDagrField;
};
createAnnotateDagrButton = function() {
    var annotateDagrButton = Ext.create('Ext.Button', {
        text: 'Annotate DAGR',
        handler: function() {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    alert(xmlhttp.responseText);
                }
            };
            var params = '?dagrId=' + encodeURIComponent(this.up('panel').getId());
            var datafield = Ext.getCmp('annotateDagrField' + this.up('panel').getId()).getRawValue();
            params += '&comments=' + encodeURIComponent(datafield);
            xmlhttp.open("GET", url + "annotateDagr" + params, true);
            xmlhttp.send();
        }
    });
    return annotateDagrButton;
};

createAnnotateDagrField = function(dagrId) {
    var annotateDagrField = Ext.create('Ext.form.field.TextArea', {
        id: 'annotateDagrField' + dagrId
    });
    return annotateDagrField;
};

createAddDagrButton = function() {
    var addDagrButton = Ext.create('Ext.Button', {
        text: 'Add file to DAGR',
        handler: function() {
            var dagrId = this.up('panel').getId();
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                        alert(xmlhttp.responseText);
                    }
                    else {
                        var tree = Ext.getCmp('treeView' + dagrId);
                        tree.getStore().setRootNode(getTreeView(dagrId));
                    }
                }
            };
            var params = '?dagrId=' + encodeURIComponent(dagrId);
            var datafield = Ext.getCmp('addDagrField' + dagrId).getRawValue();
            params += '&path=' + encodeURIComponent(datafield);
            xmlhttp.open("GET", url + "addDagr" + params, false);
            xmlhttp.send();
        }});
    return addDagrButton;
};

createReachedDagrsButton = function() {
    var findReachingDagrsButton = Ext.create('Ext.Button', {
        text: 'Find child DAGRs',
        handler: function() {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    var resultWindow = createGridWindow('Ancestors', xmlhttp.responseText);
                    resultWindow.show();
                }
            };
            var params = '?dagrId=' + encodeURIComponent(this.up('panel').getId());
            xmlhttp.open("GET", url + "findChildren" + params, true);
            xmlhttp.send();
        }});
    return findReachingDagrsButton;
};

createReachingDagrsButton = function() {
    var findReachingDagrsButton = Ext.create('Ext.Button', {
        text: 'Find ancestor DAGRs',
        handler: function() {
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    var resultWindow = createGridWindow('Children', xmlhttp.responseText);
                    resultWindow.show();
                }
            };
            var params = '?dagrId=' + encodeURIComponent(this.up('panel').getId());
            xmlhttp.open("GET", url + "findAncestors" + params, true);
            xmlhttp.send();
        }});
    return findReachingDagrsButton;
};

createRenameDagrField = function(dagrId) {
    var addDagrField = Ext.create('Ext.form.field.Text', {
        id: "renameDagrField" + dagrId
    });
    return addDagrField;
};

createRenameDagrButton = function() {
    var findReachingDagrsButton = Ext.create('Ext.Button', {
        text: 'Rename DAGR',
        handler: function() {
            var dagrId = this.up('panel').getId();
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                        alert(xmlhttp.responseText);
                    } else {
                        Ext.getCmp(dagrId).setTitle(datafield);
                        var tree = Ext.getCmp('treeView' + dagrId);
                        tree.getStore().setRootNode(getTreeView(dagrId));
                    }
                }
            };
            var params = '?dagrId=' + encodeURIComponent(dagrId);
            var datafield = Ext.getCmp('renameDagrField' + this.up('panel').getId()).getRawValue();
            params += '&name=' + encodeURIComponent(datafield);
            xmlhttp.open("GET", url + "renameDagr" + params, false);
            xmlhttp.send();
        }});
    return findReachingDagrsButton;
};

createDeleteDagrButton = function() {
    var deleteDagrButton = Ext.create('Ext.Button', {
        text: 'Delete DAGR',
        handler: function() {
            var dagrId = selections['treeView' + this.up('panel').getId()];
            var tree = Ext.getCmp('treeView' + this.up('panel').getId());
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function()
            {
                if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                {
                    var secondxmlhttp = new XMLHttpRequest();
                    secondxmlhttp.onreadystatechange = function() {
                        if (secondxmlhttp.readyState === 4 && secondxmlhttp.status === 200) {
                            var message = 'Deleting this DAGR will affect the following DAGRs.  Continue?';
                            message += grabNamesFromJsonArray(xmlhttp.responseText);
                            message += grabNamesFromJsonArray(secondxmlhttp.responseText);
                            if (message.length > 2000) {
                                message = message.substring(0, 2000);
                                message += '\n ... ';
                            }
                            var toDelete = confirm(message);
                            if (toDelete) {
                                var deletexmlhttp = new XMLHttpRequest();
                                deletexmlhttp.onreadystatechange = function() {
                                    if (deletexmlhttp.readyState === 4 && deletexmlhttp.status === 200) {
                                        tree.setRootNode(getTreeView(tree.getId().substring(8)));
                                    }
                                };
                                deletexmlhttp.open("GET", url + "deleteDagr" + params, false);
                                deletexmlhttp.send();
                            }
                        }
                    };
                    secondxmlhttp.open("GET", url + "findAncestors" + params, false);
                    secondxmlhttp.send();

                }
            };
            var params = '?dagrId=' + encodeURIComponent(dagrId);
            xmlhttp.open("GET", url + "findChildren" + params, false);
            xmlhttp.send();
            // if confirmation given, delete dagr, update treeview
        }
    });
    return deleteDagrButton;
};

/*
 * ========================================================
 * TreeView
 * ========================================================
 */

createTreeStore = function(dagrId, jsonData) {
    var store = Ext.create('Ext.data.TreeStore', {
        model: 'TreeModel',
        root: jsonData
    });
    return store;
};

createTreeView = function(dagrId, jsonData) {
    var tree = Ext.create('Ext.tree.Panel', {
        title: 'DAGR View',
        id: 'treeView' + dagrId,
        width: 400,
        height: 250,
        store: createTreeStore(dagrId, jsonData),
        rootVisible: false,
        listeners: {
            'select': function(a, record, b, c) {
                var parentPanel = this.up('panel');
                selections[this.getId()] = record.get('guid');
                if (record.get('text').substring(0, 4) === 'http') {
                    parentPanel.remove(Ext.getCmp('docViewer' + parentPanel.getId()));
                    var newViewer = createDocViewerPanel(parentPanel.getId());
                    newViewer.add({
                        xtype: 'panel',
                        html: getDocViewerHtml(record.get('text'))
                    });
                    parentPanel.add(newViewer);
                }
                else {
                    var xmlhttp = new XMLHttpRequest();
                    xmlhttp.onreadystatechange = function()
                    {
                        if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
                        {
                            parentPanel.remove(Ext.getCmp('docViewer' + parentPanel.getId()));
                            var newViewer = createDocViewerPanel(parentPanel.getId());
                            newViewer.add({
                                xtype: 'panel',
                                html: getDocViewerHtml(host + xmlhttp.responseText)
                            });
                            parentPanel.add(newViewer);

                        }
                    };
                    xmlhttp.open("POST", url + "cacheDagr", true);
                    xmlhttp.send(record.get('text'));
                }
            }
        }
    });
    return tree;
};

getTreeView = function(dagrId) {
    var xmlhttp = new XMLHttpRequest();
    var jsonObject;
    xmlhttp.onreadystatechange = function()
    {
        if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
        {
            jsonObject = eval('(' + xmlhttp.responseText + ')');
        }
    };
    var params = '?dagrId=' + encodeURIComponent(dagrId);
    xmlhttp.open("GET", url + "dagrView" + params, false);
    xmlhttp.send();
    return jsonObject;
};


/* ===================================================
 * BULK LOAD
 * ===================================================
 */

var bulkLoadField = Ext.create('Ext.form.field.Text', {
    id: "bulkLoadField"
});

var bulkLoadButton = Ext.create('Ext.Button', {
    text: 'Bulk Load',
    handler: function() {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                if (xmlhttp.responseText.substring(0, 5) === 'Error') {
                    alert(xmlhttp.responseText);
                } else
                {
                    initPanel(xmlhttp.responseText);
                }
            }
        };
        xmlhttp.open("POST", url + "bulkLoad", true);
        var param = bulkLoadField.getRawValue();
        xmlhttp.send(param);
    }
});

/* ===================================================
 * GRID window / search result formatting
 * ===================================================
 */

createGridStore = function() {
    var store = Ext.create('Ext.data.Store', {
        model: 'SingleDagrModel',
        proxy: {
            type: 'memory',
            reader: {
                type: 'json',
                root: 'items'
            }
        }
    });
    return store;
};

createGridWindow = function(title, jsonString) {
    var results = Ext.create('Ext.window.Window', {
        title: title,
        height: 600,
        width: 800,
        layout: 'fit',
        items: [Ext.create('Ext.grid.Panel', {
                store: createGridStore(),
                columns: [
                    {text: 'Name', dataIndex: 'name', flex: 1},
                    {text: 'Guid', dataIndex: 'guid'},
                    {text: 'Date', dataIndex: 'date'},
                    {text: 'Path', dataIndex: 'path'}
                ]
            })]
    });
    var dagrArray = eval('(' + jsonString + ')');
    var gridStore = results.child('panel').getStore();
    gridStore.loadData(dagrArray['items'], true);
    return results;
};

grabNamesFromJsonArray = function(dagrList) {
    var jsonArr = eval('(' + dagrList + ')');
    var nameArr = '';
    for (var i = 0; i < jsonArr['items'].length; i++) {
        nameArr += jsonArr['items'][i]['name'] + '\n';
    }
    return nameArr;
};


/*
 * ===========================================================
 * Document viewer
 * ===========================================================
 */

getDocViewerHtml = function(path) {
    var html = '<iframe src="http://docs.google.com/viewer?url=';
    html += encodeURIComponent(path);
    html += '&embedded=true" width="850" height="780" style="border: none;"></iframe>';
    return html;
};
