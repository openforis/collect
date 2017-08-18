
export default class AbstractService {

    getJson(url) {
        return fetch(url, {
            credentials: 'same-origin'
        })
        .then(response => response.json(),
            error => console.log('An error occured.', error))

    }

    postJson(url, data) {
        return fetch(url, {
            method: 'POST',
            data: data
        }).then(response => response.json(),
            error => console.log('An error occured.', error))
    }
}
    