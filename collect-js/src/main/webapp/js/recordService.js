angular.module('collectApp').
    factory('recordService', ['$http', '$q', 'eventHandlers', function ($http, $q, eventHandlers) {
        var record = {nodeById: {}};

        var dummyRecordEvents = [
            {
                type: 'EntityAdded',
                defId: 'plot',
                id: '1'
            },
            {
                type: 'AttributeAdded',
                defId: 'plot_number',
                parentId: '1',
                id: '2'
            },
            {
                type: 'EntityListAdded',
                defId: 'trees',
                id: '1-3',
                parentId: '1'
            },
            {
                type: 'EntityAdded',
                defId: 'tree',
                parentId: '1-3',
                id: '4'
            },
            {
                type: 'AttributeAdded',
                defId: 'tree_number',
                parentId: '4',
                id: '5',
                value: 'A test tree number'
            },
            {
                type: 'EntityAdded',
                defId: 'tree',
                id: '6',
                parentId: '1-3'
            },
            {
                type: 'AttributeAdded',
                defId: 'tree_number',
                id: '7',
                parentId: '6'
            }
        ];

        function handleEvent(event) {
            var handler = eventHandlers[event.type];
            handler(record, event);
        }

        function handleEvents(events) {
            angular.forEach(events, function (event) {
                handleEvent(event);
            });
        }

        function initSchema(schema) {
            schema.defById = {};
            registerDefinitions(schema, schema.defById);
            return schema;
        }

        function registerDefinitions(def, defById) {
            defById[def.id] = def;
            if (def.member)
                registerDefinitions(def.member, defById);
            if (def.members)
                angular.forEach(def.members, function (member) {
                    registerDefinitions(member, defById);
                });
        }

        function loadSchemaAndRecord(recordId) {
            var deferred = $q.defer();

            $q.all([loadSchema(), loadRecord(recordId)]).
                then(function (result) {
                    record.schema = initSchema(result[0].data);
                    var events = result[1].data;
                    handleEvents(dummyRecordEvents);
                    //handleEvents(events);
                    deferred.resolve(record);
                }).
                catch(function (data, status, headers, config) {
                    deferred.reject("Failed"); // TODO: Need to provide some details on the error here
                });

            return deferred.promise;
        }

        function loadSchema() {
            return $http.get('json/schema');
        }


        function loadRecord(recordId) {
            return $http.get('json/record', {
                params: {id: recordId}
            });
        }

        function updateAttribute(attribute) {
            $http.post('json/update-attribute', attribute)
        }

        return {
            'loadRecord': loadSchemaAndRecord,
            'updateAttribute': updateAttribute
        };
    }]);