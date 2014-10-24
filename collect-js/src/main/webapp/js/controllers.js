var collectApp = angular.module('collectApp', []).

    controller('RecordCtrl', ['$scope', '$http', 'recordService',
        function ($scope, $http, recordService) {
            recordService.loadRecord(123).then(function (record) {
                $scope.record = record;
                console.log("loaded record", $scope.record);
            });
        }]).

    directive('renderNode', ['recordService', function (recordService) {
        return {
            restrict: 'E',
            scope: {
                node: '='
            },
            template: "<div ng-include src=\"'partials/' + node.type + '.html'\"></div>",
            link: function ($scope, element, attrs) {
                $scope.attributeUpdated = function (attribute) {
                    recordService.updateAttribute(attribute);
                }
            }
        };
    }]);