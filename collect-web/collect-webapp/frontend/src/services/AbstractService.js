import Constants from '../utils/Constants';

export default class AbstractService {

    BASE_URL = Constants.API_BASE_URL;
    
    get(url) {
        return fetch(this.BASE_URL + url, {
            credentials: 'same-origin'
        })
        .then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
    }

    post(url, data) {
        const queryData = Object.keys(data).map((key) => {
            let val = data[key];
            let encodedVal = val === null ? '' : encodeURIComponent(val);
            return encodeURIComponent(key) + '=' + encodedVal;
          }).join('&');
          
        return fetch(this.BASE_URL + url, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: queryData
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
        .catch(error => {
            throw(error);
        })
        }
}
    