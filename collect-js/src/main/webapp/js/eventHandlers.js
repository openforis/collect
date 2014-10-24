angular.module('collectApp').
    factory('eventHandlers', function () {
        return {

            EntityAdded: function (record, event) {
                var node = addNode(record, event, 'Entity');
                node.members = [];
            },

            EntityListAdded: function (record, event) {
                var node = addNode(record, event, 'EntityList');
                node.members = [];
            },

            AttributeAdded: function (record, event) {
                var node = addNode(record, event, 'Attribute');
                node.value = event.value;
            }
        };

        function addNode(record, event, type) {
            var def = definition(record, event);
            var node = {
                type: type,
                id: event.id,
                label: def.label
            };
            record.nodeById[node.id] = node;

            if (event.parentId)
                record.nodeById[event.parentId].members.push(node);
            else
                record.rootEntity = node;

            return node;
        }

        function definition(record, event) {
            return record.schema.defById[event.defId];
        }
    });
