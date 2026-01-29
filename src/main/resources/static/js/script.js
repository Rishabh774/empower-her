// EmpowerHer - Custom JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    initializeTooltips();
    initializeBookmarkButtons();
    setupSearchFunctionality();
    setupFormValidations();
    setupAutoDismissAlerts();
    setupSmoothScrolling();
}

// Tooltip Initialization
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Bookmark Functionality
function initializeBookmarkButtons() {
    document.querySelectorAll('.bookmark-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const schemeId = this.dataset.schemeId;
            if (schemeId) {
                toggleBookmark(schemeId, this);
            }
        });
    });
}

function toggleBookmark(schemeId, button) {
    // Show loading state
    const originalHTML = button.innerHTML;
    button.innerHTML = '<div class="loading-spinner"></div>';
    button.disabled = true;

    fetch(`/user/bookmark/${schemeId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update button appearance
            if (data.bookmarked) {
                button.classList.remove('btn-outline-warning');
                button.classList.add('btn-warning');
                button.innerHTML = '<i class="fas fa-bookmark"></i>';
                showToast('Bookmark added successfully!', 'success');
            } else {
                button.classList.remove('btn-warning');
                button.classList.add('btn-outline-warning');
                button.innerHTML = '<i class="far fa-bookmark"></i>';
                showToast('Bookmark removed!', 'info');
            }
            
            // Update tooltip
            updateButtonTooltip(button, data.bookmarked);
        } else {
            showToast('Error: ' + data.error, 'error');
            button.innerHTML = originalHTML;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('An error occurred. Please try again.', 'error');
        button.innerHTML = originalHTML;
    })
    .finally(() => {
        button.disabled = false;
    });
}

function updateButtonTooltip(button, isBookmarked) {
    const tooltip = bootstrap.Tooltip.getInstance(button);
    if (tooltip) {
        tooltip.dispose();
    }
    button.setAttribute('title', isBookmarked ? 'Remove bookmark' : 'Add bookmark');
    new bootstrap.Tooltip(button);
}

// Search Functionality
function setupSearchFunctionality() {
    const searchInput = document.getElementById('searchInput');
    const searchForm = document.querySelector('form[action*="search"]');
    
    if (searchInput && searchForm) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });
    }
}

function performSearch(query) {
    if (query.trim() !== '') {
        window.location.href = `/search?query=${encodeURIComponent(query)}`;
    }
}

// Form Validations
function setupFormValidations() {
    const forms = document.querySelectorAll('form[needs-validation]');
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
}

// Auto-dismiss Alerts
function setupAutoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        if (!alert.classList.contains('alert-permanent')) {
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 5000);
        }
    });
}

// Smooth Scrolling
function setupSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Toast Notifications
function showToast(message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }

    // Create toast element
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas ${getToastIcon(type)} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    // Show toast
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
    toast.show();

    // Remove toast from DOM after hide
    toastElement.addEventListener('hidden.bs.toast', function () {
        this.remove();
    });
}

function getToastIcon(type) {
    const icons = {
        'success': 'fa-check-circle',
        'error': 'fa-exclamation-triangle',
        'warning': 'fa-exclamation-circle',
        'info': 'fa-info-circle'
    };
    return icons[type] || 'fa-info-circle';
}

// CSRF Token Helper
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
}

// File Upload Preview
function setupFilePreview(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    
    if (input && preview) {
        input.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                    preview.style.display = 'block';
                }
                reader.readAsDataURL(file);
            }
        });
    }
}

// Character Counter
function setupCharacterCounter(textareaId, counterId, maxLength) {
    const textarea = document.getElementById(textareaId);
    const counter = document.getElementById(counterId);
    
    if (textarea && counter) {
        textarea.addEventListener('input', function() {
            const remaining = maxLength - this.value.length;
            counter.textContent = `${remaining} characters remaining`;
            
            if (remaining < 0) {
                counter.classList.add('text-danger');
            } else {
                counter.classList.remove('text-danger');
            }
        });
    }
}

// Password Strength Checker
function checkPasswordStrength(password) {
    let strength = 0;
    
    if (password.length >= 8) strength++;
    if (password.match(/[a-z]+/)) strength++;
    if (password.match(/[A-Z]+/)) strength++;
    if (password.match(/[0-9]+/)) strength++;
    if (password.match(/[$@#&!]+/)) strength++;
    
    return strength;
}

// Export functions for global use
window.EmpowerHer = {
    toggleBookmark,
    showToast,
    performSearch,
    checkPasswordStrength
};







// Add these functions to your existing script.js

// Share functionality
function shareScheme(platform, schemeId, schemeTitle, button) {
    const currentUrl = window.location.href;
    const shareUrls = {
        facebook: `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(currentUrl)}&quote=${encodeURIComponent(schemeTitle)}`,
        twitter: `https://twitter.com/intent/tweet?url=${encodeURIComponent(currentUrl)}&text=${encodeURIComponent(schemeTitle)}&hashtags=EmpowerHer,WomenSchemes`,
        whatsapp: `https://wa.me/?text=${encodeURIComponent(schemeTitle + ' - ' + currentUrl)}`,
        email: `mailto:?subject=${encodeURIComponent(schemeTitle)}&body=${encodeURIComponent('Check out this women empowerment scheme: ' + currentUrl)}`
    };

    // Open share window
    if (platform === 'email') {
        window.location.href = shareUrls[platform];
    } else {
        window.open(shareUrls[platform], '_blank', 'width=600,height=400');
    }

    // Increment share count
    incrementShareCount(schemeId);

    // Show success message
    showToast('Scheme shared successfully!', 'success');
}

function incrementShareCount(schemeId) {
    fetch(`/api/share/increment/${schemeId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update share count display
            const shareCountElements = document.querySelectorAll('.share-count');
            shareCountElements.forEach(el => {
                el.textContent = data.shareCount;
            });
            
            // Update badge if exists
            const shareBadge = document.querySelector('.share-badge');
            if (shareBadge) {
                shareBadge.innerHTML = `<i class="fas fa-share"></i> ${data.shareCount} shares`;
            }
        }
    })
    .catch(error => {
        console.error('Error updating share count:', error);
    });
}

function copySchemeLink(schemeId, button) {
    const currentUrl = window.location.href;
    
    navigator.clipboard.writeText(currentUrl).then(() => {
        // Show copied feedback
        const originalText = button.innerHTML;
        button.innerHTML = '<i class="fas fa-check me-2"></i>Copied!';
        button.classList.remove('btn-outline-secondary');
        button.classList.add('btn-success');
        
        showToast('Link copied to clipboard!', 'success');
        
        // Revert button after 2 seconds
        setTimeout(() => {
            button.innerHTML = originalText;
            button.classList.remove('btn-success');
            button.classList.add('btn-outline-secondary');
        }, 2000);

        // Increment share count
        incrementShareCount(schemeId);
    }).catch(err => {
        console.error('Failed to copy: ', err);
        showToast('Failed to copy link', 'error');
    });
}

// Initialize share buttons
function initializeShareButtons() {
    document.querySelectorAll('.share-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const platform = this.dataset.platform;
            const schemeId = this.dataset.schemeId;
            const schemeTitle = this.dataset.schemeTitle;
            shareScheme(platform, schemeId, schemeTitle, this);
        });
    });

    document.querySelectorAll('.copy-link-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const schemeId = this.dataset.schemeId;
            copySchemeLink(schemeId, this);
        });
    });
}

// Call this in DOMContentLoaded
document.addEventListener('DOMContentLoaded', function() {
    initializeShareButtons();
    // ... other initializations
});







// Add these functions to script.js

// Comments functionality
function initializeComments(schemeId) {
    loadComments(schemeId);
    
    // Comment form submission
    const commentForm = document.getElementById('commentForm');
    if (commentForm) {
        commentForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitComment(schemeId);
        });
    }
}

function loadComments(schemeId) {
    fetch(`/api/comments/scheme/${schemeId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayComments(data.comments);
                updateCommentCount(data.commentCount);
            } else {
                showToast('Error loading comments: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error loading comments:', error);
            showToast('Error loading comments', 'error');
        });
}

function displayComments(comments) {
    const commentsList = document.getElementById('commentsList');
    const noComments = document.getElementById('noComments');
    
    if (comments.length === 0) {
        commentsList.classList.add('d-none');
        noComments.classList.remove('d-none');
        return;
    }
    
    noComments.classList.add('d-none');
    commentsList.classList.remove('d-none');
    
    let commentsHTML = '';
    
    comments.forEach(comment => {
        const commentDate = new Date(comment.createdAt).toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        
        commentsHTML += `
            <div class="card mb-3 comment-item" data-comment-id="${comment.id}">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div class="d-flex align-items-center">
                            <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3" 
                                 style="width: 40px; height: 40px; font-size: 14px;">
                                ${comment.user.username.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <h6 class="mb-0">${comment.user.username}</h6>
                                <small class="text-muted">${commentDate}</small>
                            </div>
                        </div>
                        <div class="comment-actions">
                            <button class="btn btn-sm btn-outline-secondary edit-comment-btn" 
                                    data-comment-id="${comment.id}"
                                    data-content="${comment.content.replace(/"/g, '&quot;')}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger delete-comment-btn" 
                                    data-comment-id="${comment.id}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                    <p class="card-text comment-content">${comment.content}</p>
                </div>
            </div>
        `;
    });
    
    commentsList.innerHTML = commentsHTML;
    
    // Add event listeners for edit and delete buttons
    document.querySelectorAll('.edit-comment-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const commentId = this.dataset.commentId;
            const content = this.dataset.content;
            editComment(commentId, content);
        });
    });
    
    document.querySelectorAll('.delete-comment-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const commentId = this.dataset.commentId;
            deleteComment(commentId);
        });
    });
}

function submitComment(schemeId) {
    const content = document.getElementById('commentContent').value.trim();
    const submitBtn = document.querySelector('#commentForm button[type="submit"]');
    
    if (!content) {
        showToast('Please enter a comment', 'warning');
        return;
    }
    
    // Show loading state
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Posting...';
    submitBtn.disabled = true;
    
    fetch(`/api/comments/scheme/${schemeId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: content })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('commentContent').value = '';
            showToast('Comment posted successfully!', 'success');
            loadComments(schemeId); // Reload comments
        } else {
            showToast('Error posting comment: ' + data.error, 'error');
        }
    })
    .catch(error => {
        console.error('Error posting comment:', error);
        showToast('Error posting comment', 'error');
    })
    .finally(() => {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    });
}

function editComment(commentId, currentContent) {
    const newContent = prompt('Edit your comment:', currentContent);
    
    if (newContent !== null && newContent.trim() !== '' && newContent !== currentContent) {
        fetch(`/api/comments/${commentId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ content: newContent.trim() })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Comment updated successfully!', 'success');
                // Reload the scheme page comments
                const schemeId = window.location.pathname.split('/').pop();
                loadComments(schemeId);
            } else {
                showToast('Error updating comment: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error updating comment:', error);
            showToast('Error updating comment', 'error');
        });
    }
}

function deleteComment(commentId) {
    if (confirm('Are you sure you want to delete this comment?')) {
        fetch(`/api/comments/${commentId}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Comment deleted successfully!', 'success');
                // Reload the scheme page comments
                const schemeId = window.location.pathname.split('/').pop();
                loadComments(schemeId);
            } else {
                showToast('Error deleting comment: ' + data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting comment:', error);
            showToast('Error deleting comment', 'error');
        });
    }
}

function updateCommentCount(count) {
    // Update all comment count elements
    document.querySelectorAll('.comment-count').forEach(el => {
        el.textContent = count;
    });
}

// Update DOMContentLoaded to initialize comments
document.addEventListener('DOMContentLoaded', function() {
    // ... existing code ...
    
    // Initialize comments if on scheme detail page
    const schemeId = window.location.pathname.split('/').pop();
    if (window.location.pathname.includes('/scheme/') && schemeId) {
        initializeComments(schemeId);
    }
});