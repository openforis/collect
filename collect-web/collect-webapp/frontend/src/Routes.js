const routes = [
  {
    path: '/',
    name: 'Home'
  },
  {
    path: '/home',
    name: 'Home'
  },
  {
    path: '/signin',
    name: "Signin"
  },
  {
    path: '/dashboard',
    name: "Dashboard",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement',
    name: "Data Management",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/export',
    name: "Export Data",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/csvexport',
    name: "Export to CSV",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/backup',
    name: "Backup data",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/csvimport',
    name: "Import Data from CSV/Excel",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/backupimport',
    name: "Import Backup file",
    surveySelectRequired: true
  },
  {
    path: '/datamanagement/:id',
    name: 'Record',
    pathRegExp: new RegExp('/datamanagement/(\\d)+'),
    surveySelectRequired: true
  },
  {
    path: '/surveydesigner',
    name: "Survey Designer"
  },
  {
    path: '/surveydesigner/surveys',
    name: "List of surveys"
  },
  {
    path: '/surveydesigner/surveys/export/:id',
    name: 'Export survey',
    pathRegExp: new RegExp('/surveydesigner/surveys/export/(\\d)+')
  },
  {
    path: '/surveydesigner/newsurvey',
    name: "New survey"
  },
  {
    path: '/surveydesigner/surveyimport',
    name: "Import survey"
  },
  {
    path: '/surveydesigner/surveys/:id',
    name: "Edit survey",
    pathRegExp: new RegExp('/surveydesigner/surveys/(\\d)+')
  },
  {
    path: '/datacleansing',
    name: "Data Cleansing"
  },
  {
    path: '/map',
    name: "Map"
  },
  {
    path: '/saiku',
    name: "Saiku",
    surveySelectRequired: true
  },
  {
    path: '/users',
    name: "Users"
  },
  {
    path: '/usergroups',
    name: "User Groups"
  },
  {
    path: '/usergroups/:id',
    name: 'User Group',
    pathRegExp: new RegExp('/usergroups/(\\d)+')
  },
  {
    path: '/usergroups/new',
    name: 'New User Group'
  }
];

export default class Routes {

  static findRouteByPath(path) {
    let route = routes.find(route => {
      if (route.path === path) {
        return true;
      } else if (route.pathRegExp && route.pathRegExp.test(path)) {
          return true;
      } else {
        return false;
      }
    });
    return route
  }

  static findRouteNameByPath(path) {
    const route = Routes.findRouteByPath(path)
    return route ? route.name: undefined
  }

  static isSurveySelectRequiredForPath(path) {
    const route = Routes.findRouteByPath(path)
    return route ? route['surveySelectRequired'] === true : false
  }
}
