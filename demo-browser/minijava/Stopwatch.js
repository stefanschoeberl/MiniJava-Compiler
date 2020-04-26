module.exports = runtime => {
    const start = new Date().getTime();
    return {
        'Stopwatch.now': () => {
            return new Date().getTime() - start;
        }
    };
};