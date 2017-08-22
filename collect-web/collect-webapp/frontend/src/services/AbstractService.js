
export default class AbstractService {

    get(url) {
        return fetch(url, {
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
            return encodeURIComponent(key) + '=' + encodeURIComponent(data[key]);
          }).join('&');
          
        return fetch(url, {
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
    