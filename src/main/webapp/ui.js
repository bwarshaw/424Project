var tabInterface;
var url = "http://localhost:8084/MMDA424/rest/webservice/";
var selections = new Array();

Ext.define('TreeModel', {
    extend: 'Ext.data.Model',
    fields: ['text', 'guid']
});



Ext.onReady(function() {
    var initConnReq = new XMLHttpRequest();
    initConnReq.open("POST", url + "initConnection", false);
    initConnReq.send();

    tabInterface = Ext.create('Ext.tab.Panel', {
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
                    openTabButton, openTabField]
            }
        ]
    });
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
                    var newTab = initPanel(xmlhttp.responseText);
                    tabInterface.add(newTab);
                    tabInterface.setActiveTab(newTab);
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
                for (var treeId in selections) {
                    var tree = Ext.getCmp(treeId);
                    var dagrId = treeId.substring(8);
                    tree.getStore().setRootNode(getTreeView(dagrId));
                }
                Ext.create('Ext.window.Window', {
                    title: 'Records Condensed',
                    height: 450,
                    width: 600,
                    layout: 'fit',
                    overflowY: 'auto',
                    html: formatString(xmlhttp.responseText)
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
                Ext.create('Ext.window.Window', {
                    title: 'Search Results - Orphaned DAGRs',
                    height: 450,
                    width: 600,
                    layout: 'fit',
                    overflowY: 'auto',
                    html: formatString(xmlhttp.responseText)
                }).show();
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
                Ext.create('Ext.window.Window', {
                    title: 'Search Results - Sterile DAGRs',
                    height: 450,
                    width: 600,
                    layout: 'fit',
                    overflowY: 'auto',
                    html: formatString(xmlhttp.responseText)
                }).show();
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
                    var newTab = initPanel(xmlhttp.responseText);
                    tabInterface.add(newTab);
                    tabInterface.setActiveTab(newTab);
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
    text: 'Keywords (split by ,)'
});


var searchDagrsButton = Ext.create('Ext.Button', {
    text: 'Search DAGRs',
    handler: function() {
        var fromD = fromDateField.getRawValue();
        var toD = toDateField.getRawValue();
        var name = searchNameField.getRawValue();
        var type = searchTypeField.getRawValue();
        var comment = searchCommentField.getRawValue();
        var keywords = comment.split(',');
        var minSize = minSizeField.getRawValue();
        var maxSize = maxSizeField.getRawValue();
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function()
        {
            if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
            {
                Ext.create('Ext.window.Window', {
                    title: 'Search Results',
                    height: 450,
                    width: 600,
                    layout: 'fit',
                    overflowY: 'auto',
                    html: formatString(xmlhttp.responseText)
                }).show();
            }
        };
        var params = '?fromDate=' + encodeURIComponent(fromD);
        params += '&toDate=' + encodeURIComponent(toD);
        params += '&name=' + encodeURIComponent(name);
        params += '&type=' + encodeURIComponent(type);
        params += '&minSize=' + encodeURIComponent(minSize);
        params += '&maxSize=' + encodeURIComponent(maxSize);
        params += '&keywords=' + encodeURIComponent(keywords);
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
    var jsonData = eval('(' + dagrData + ')');
    var name = jsonData['name'];
    var guid = jsonData['guid'];
    var rootNode = getTreeView(guid);
    var tree = createTreeView(guid, rootNode);
    var panel = Ext.create('Ext.panel.Panel', {
        title: name,
        id: guid,
        closable: true,
        items: [createAddDagrButton(), createAddDagrField(guid),
            createAnnotateDagrButton(), createAnnotateDagrField(guid),
            createReachedDagrsButton(), createReachingDagrsButton(),
            createRenameDagrButton(), createRenameDagrField(guid), createDeleteDagrButton(), tree]
    });
    return panel;
}
;


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
                    Ext.create('Ext.window.Window', {
                        title: 'Search Results - Reached DAGRs',
                        height: 450,
                        width: 600,
                        layout: 'fit',
                        overflowY: 'auto',
                        html: formatString(xmlhttp.responseText)
                    }).show();
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
                    Ext.create('Ext.window.Window', {
                        title: 'Search Results - Reaching DAGRs',
                        height: 450,
                        width: 600,
                        layout: 'fit',
                        overflowY: 'auto',
                        html: formatString(xmlhttp.responseText)
                    }).show();
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
                        Ext.getCmp(this.up('panel').getId()).setTitle(datafield);
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
                            message += xmlhttp.responseText;
                            message += secondxmlhttp.responseText;
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
                selections[this.getId()] = record.get('guid');
//                alert(record.get('text') + ' -> ' + record.get('guid'));
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
                    var newTab = initPanel(xmlhttp.responseText);
                    tabInterface.add(newTab);
                    tabInterface.setActiveTab(newTab);
                }
            }
        };
        xmlhttp.open("POST", url + "bulkLoad", true);
        var param = bulkLoadField.getRawValue();
        xmlhttp.send(param);
    }
});

formatString = function(str) {
    return str.replace(/@@@/g, '<br>');
};